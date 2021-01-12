package tk.leoforney.smartdoorpi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Leo on 4/10/2017.
 */

@SpringBootApplication
public class Main {

    private List<Door> doors;
    private DeviceConfig config;

    public Main() {
        config = Options.grabConfig();

        doors = Options.grabConfig().doors;

        Logger.getLogger(Main.class.getSimpleName()).info("Starting with device " + config.deviceName + ". " +
                "Loading " + doors.size() + " doors.");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}
