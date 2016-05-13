package aut.pokimin_battlearena.services;

import android.app.Activity;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public interface BluetoothNode extends Runnable, Serializable {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // FIELDS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // uuid for the Bluetooth application
    public static final UUID SERVICE_UUID = UUID.fromString("aa7e561f-591f-4767-bf26-e4bff3f0875f");
    // name for the Bluetooth application
    public static final String SERVICE_NAME = "Bluetooth";

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // METHODS
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // forward a message to all chat nodes in the Bluetooth network
    public void forward(String message);
    // stop this chat node and clean up
    public void stop();
    // registers or unregisters (if null) a ChatActivity for display
    public void registerActivity(Activity activity);

}
