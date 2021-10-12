#include <iostream>
#include <wiringPi.h>
#include <fstream>
#include <unistd.h>
#include <pwd.h>
#include "json.hpp"
#include "door.h"
#include <curl/curl.h>

std::string getConfigFileFromArgs(int argc, char **pString);

void sendDoorStatus(ns::door &door, const std::string& endpoint, const std::string& apiKey);

using json = nlohmann::json;

namespace ns {
    void to_json(json& j, const door& d) {
        j = json{{"name", d.name}, {"codeName", d.codeName}, {"current", d.current}, {"pin", d.pin}};
    }

    void from_json(const json& j, door& d) {
        j.at("name").get_to(d.name);
        j.at("codeName").get_to(d.codeName);
        j.at("current").get_to(d.current);
        j.at("pin").get_to(d.pin);
    }
}

std::string getConfigFileFromArgs(int argc, char **pString) {
    std::string fileLocation = "config.json";
    for (int counter = 0; counter < argc; counter++) {
        if (strcmp(pString[counter], "-c") == 0 || strcmp(pString[counter], "--config") == 0) {
            if (argc >= counter + 1) {
                return pString[counter + 1];
            } else {
                std::cout << "Config flag specified but file not given" << std::endl;
                std::cout << "Using default config.json in working directory" << std::endl;
                return fileLocation;
            }
        }
    }

    return fileLocation;
}

int main(int argc, char** argv) {
    std::cout << "Opening SmartDoorPi v2.0" << std::endl;

    std::string configFileName = getConfigFileFromArgs(argc, argv);
    std::cout << "Loading config file from " << configFileName << std::endl;

    std::string line;
    std::string configFileContents;
    std::ifstream configFile(configFileName);
    if (configFile.is_open()) {
        while (getline(configFile, line)) {
            configFileContents.append(line);
        }
        configFile.close();
    } else {
        std::cerr << "Can't open configuration file" << std::endl;
        return -1;
    }

    if (configFileContents.empty()) {
        std::cerr << "Config file empty" << std::endl;
        return -1;
    }

    auto conf = json::parse(configFileContents);

    std::string apiKey = conf["smartthingsApiKey"].get<std::string>();
    std::string smartthingsEndpoint = conf["smartthingsEndpoint"].get<std::string>();
    std::cout << "Smartthings API Key: " << apiKey << std::endl;

    std::vector<ns::door> doorsVector = conf["doors"];

    wiringPiSetup();

    for (auto & i : doorsVector) {
        std::cout << "Setting up "
                << i.name
                << " on pin "
                << i.pin <<
                std::endl;
        pinMode(i.pin, INPUT);
        pullUpDnControl(i.pin, PUD_DOWN);
    }

    bool firstRun = true;
    while (true) {
        for (auto & i : doorsVector) {
            i.prev = i.current;
            i.current = !digitalRead(i.pin);
            if (i.prev != i.current || firstRun) {
                std::cout << i.name << " has changed to " << i.current << std::endl;
                sendDoorStatus(i, smartthingsEndpoint, apiKey);
            }
        }

        if (firstRun)
            firstRun = false;

        delay(500);
    }

    return 0;
}

void sendDoorStatus(ns::door &door, const std::string& endpoint, const std::string& apiKey) {
    CURL *curl;
    CURLcode res;

    /* In windows, this will init the winsock stuff */
    curl_global_init(CURL_GLOBAL_ALL);

    /* get a curl handle */
    curl = curl_easy_init();
    if(curl) {

        struct curl_slist *chunk = NULL;

        std::string authHeader = "Authorization: Bearer ";
        authHeader.append(apiKey);

        chunk = curl_slist_append(chunk, authHeader.c_str());

        std::string compiledEndpoint = endpoint;
        compiledEndpoint.append("/doors/");
        compiledEndpoint.append(door.codeName);
        if (door.current) {
            compiledEndpoint.append("/open");
        } else {
            compiledEndpoint.append("/close");
        }

        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, chunk);
        curl_easy_setopt(curl, CURLOPT_URL, compiledEndpoint.c_str());
        curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, "PUT");

        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, "Test content");

        /* Perform the request, res will get the return code */
        res = curl_easy_perform(curl);

        /* Check for errors */
        if(res != CURLE_OK) {
            std::cerr << "curl_easy_perform() error: " << curl_easy_strerror(res) << std::endl;
        }

        /* always cleanup */
        curl_easy_cleanup(curl);
    }
    curl_global_cleanup();
}