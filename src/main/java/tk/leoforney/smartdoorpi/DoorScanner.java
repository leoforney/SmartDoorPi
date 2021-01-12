package tk.leoforney.smartdoorpi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.WiringPiGpioProviderBase;
import com.pi4j.wiringpi.GpioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Service
@Order(0)
@EnableScheduling
public class DoorScanner {

    private List<DoorChangeListener> listeners;
    private List<Door> doors;
    private GpioController controller;
    private Scanner scanner;
    private Logger logger = Logger.getLogger(DoorScanner.class.getSimpleName());

    public DoorScanner() {
        listeners = new ArrayList<>();
        doors = Options.grabConfig().doors;

        if (!Options.EMULATE) {
            GpioUtil.enableNonPrivilegedAccess();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller = GpioFactory.getInstance();
        } else {
            scanner = new Scanner(System.in);
        }

        scanDoors();

        if (Options.EMULATE) {
            new Thread(() -> {
                if (scanner != null) {
                    while (Thread.currentThread().isAlive()) {
                        refreshData(scanner.nextInt());
                    }
                }
            }).start();
        }

    }

    public void addDoorChangeListener(DoorChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeDoorChangeListener(DoorChangeListener listener) {
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i).equals(listener)) {
                listeners.remove(listener);
            }
        }
    }

    @Scheduled(fixedRate = 500)
    private void scanDoors() {
        refreshData();

        for (Door door : doors) {
            if (door.previous == null) {
                door.previous = door.current;
                try {
                    notifyListenersOfValueAdded((Door) door.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            } else {
                if (door.current != door.previous) {
                    try {
                        notifyListenersOfChange((Door) door.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    door.previous = door.current;
                }
            }
        }
    }

    private void notifyListenersOfChange(final Door door) {
        for (final DoorChangeListener listener : listeners) {
            if (listener != null) {
                listener.onDoorValueChanged(door);
            }
        }
    }

    private void notifyListenersOfValueAdded(final Door door) {
        for (final DoorChangeListener listener : listeners) {
            if (listener != null) {
                listener.onDoorValueAdded(door);
            }
        }
    }

    private void refreshData() {
        refreshData(Integer.MIN_VALUE);
    }

    private void refreshData(int doorFlop) {
        for (Door door : doors) {
            if (!Options.EMULATE) {
                door.pinInput = controller.provisionDigitalInputPin(door.doorPin);
                door.current = door.pinInput.isLow();
                controller.shutdown();
                controller.unprovisionPin(door.pinInput);
            } else {
                if (door.previous == null) {
                    door.previous = true;
                    door.current = true;
                }
            }
        }

        if (doorFlop < doors.size() && doorFlop >= 0) {
            logger.info("Switching door: " + doors.get(doorFlop).name);
            doors.get(doorFlop).current = !doors.get(doorFlop).current;
        }
    }

    public interface DoorChangeListener {
        void onDoorValueChanged(Door door);

        void onDoorValueAdded(Door door);
    }
}
