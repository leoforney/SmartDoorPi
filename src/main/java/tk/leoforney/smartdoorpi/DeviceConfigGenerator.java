package tk.leoforney.smartdoorpi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pi4j.io.gpio.RaspiPin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Leo on 4/17/2017.
 */
public class DeviceConfigGenerator {
    public static void main(String args[]) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        DeviceConfig config = new DeviceConfig();
        config.deviceName = "RPI2-Node-1";
        config.doors = new ArrayList<>(Arrays.asList(
                new Door("Patio Door", RaspiPin.GPIO_24),
                new Door("Front Door", RaspiPin.GPIO_25),
                new Door("Garage Door", RaspiPin.GPIO_26),
                new Door("Front Left Door", RaspiPin.GPIO_27),
                new Door("Front Right Door", RaspiPin.GPIO_28)));
        System.out.println(gson.toJson(config));
    }
}
