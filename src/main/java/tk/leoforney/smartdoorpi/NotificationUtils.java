package tk.leoforney.smartdoorpi;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Created by Leo on 12/16/2016.
 */

@Component
@Order(2)
public class NotificationUtils implements DoorScanner.DoorChangeListener {

    private Logger logger = Logger.getLogger(NotificationUtils.class.getSimpleName());

    @Autowired
    DoorScanner scanner;

    public NotificationUtils() {
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerDoorChangeListener() {
        scanner.addDoorChangeListener(this);
        logger.info("Registered SmartThings door hooks");
    }

    public static String OpenOrClosed(boolean open, boolean presentTense) {
        String returnValue = "";

        if (open) {
            returnValue = "open";
        }
        if (!open) {
            if (!presentTense) {
                returnValue = "closed";
            }
            if (presentTense) {
                returnValue = "close";
            }
        }

        return returnValue;
    }

    public void updateToSmartThings(Door door) {

        HttpResponse<String> resp = Unirest.put(Options.SmartThingsEndpoint + "/doors/" + door.codeName + "/" + OpenOrClosed(door.current, true))
                .header("Authorization", "Bearer " + Options.grabConfig().smartthingsApiKey)
                .body("Resource content")
                .connectTimeout(1500)
                .socketTimeout(1500)
                .asString();

        logger.info("SmartThings: [url: " + Options.SmartThingsEndpoint + "/doors/" + door.codeName + "/" + OpenOrClosed(door.current, true) + " ]");
        logger.info("Response code: " + resp.getStatus());
        String body = resp.getBody();
        if (body != null && !body.isEmpty()) logger.info(resp.getBody());

    }

    @Override
    public void onDoorValueChanged(Door door) {
        updateToSmartThings(door);
    }

    @Override
    public void onDoorValueAdded(Door door) {

    }
}
