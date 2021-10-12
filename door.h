//
// Created by Leo on 7/5/2021.
//

#ifndef SMARTDOORPI_DOOR_H
#define SMARTDOORPI_DOOR_H

#include <string>
#include "json.hpp"

using json = nlohmann::json;

namespace ns {
    struct door {
        std::string name;
        std::string codeName;
        bool current;
        int pin;
        bool prev;
    };
}

#endif //SMARTDOORPI_DOOR_H
