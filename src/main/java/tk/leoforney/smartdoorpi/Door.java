package tk.leoforney.smartdoorpi;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;

/**
 * Created by Leo on 12/16/2016.
 */
public class Door {

    public Door(String name, String codeName, Pin pin) {
        this.name = name;
        this.codeName = codeName;
        this.doorPin = pin;
        this.pinName = pin.getName();
    }

    public Door(String name, Pin pin) {
        this(name, name.replaceAll(" ", ""), pin);
    }

    public transient GpioPinDigitalInput pinInput;
    String name; // Human readable name
    String codeName; // The codename for Firebase and SmartThings like PatioDoor
    public Boolean previous;
    public boolean current; // The previous and current value for door
    public transient Pin doorPin; // The pin for the GPIO of the door
    String pinName;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCurrent(boolean open) {
        this.current = open;
    }

    public boolean getCurrent() {
        return current;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        Door cloned = new Door(name, codeName, doorPin);
        cloned.current = current;
        cloned.previous = previous;
        return cloned;
    }
}
