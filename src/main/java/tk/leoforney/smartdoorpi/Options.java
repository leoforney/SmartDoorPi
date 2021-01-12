package tk.leoforney.smartdoorpi;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.pi4j.io.gpio.RaspiPin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Created by Leo on 8/17/2016.
 */

class Options {
    static String deviceName = "N/S";
    static String appDir = "/home/pi";
    //static String appDir = "C:\\Users\\Leo\\OneDrive\\Desktop\\";
    static final String configFileName = appDir + "/config.json";
    static final String SmartThingsEndpoint = "https://graph.api.smartthings.com:443/api/smartapps/installations/a78c2690-d21f-4b79-9e48-926688e2643a";

    public static final boolean EMULATE = false;

    public static DeviceConfig grabConfig() {
        Gson gson = new Gson();
        File configFile = new File(configFileName);
        String configString = null;
        try {
            configString = Files.toString(configFile, Charset.defaultCharset());
        } catch (IOException e) {
            Logger.getLogger("ConfigGrabber", "Cannot find config.json. Are you sure it exists?");
        }
        DeviceConfig config = gson.fromJson(configString, DeviceConfig.class);
        deviceName = config.deviceName;
        for (Door door : config.doors) {
            door.doorPin = RaspiPin.getPinByName(door.pinName);
        }
        return config;
    }

}


