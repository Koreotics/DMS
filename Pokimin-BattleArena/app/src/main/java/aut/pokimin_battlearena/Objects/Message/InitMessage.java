package aut.pokimin_battlearena.Objects.Message;

import java.io.Serializable;
import java.util.HashMap;

import aut.pokimin_battlearena.Objects.Player;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class InitMessage extends AbstractMessage {

    private String serverPlayerName;
    private String clientPlayerName;
    HashMap<String, String> serverMonInfo;
    HashMap<String, String> clientMonInfo;

    public InitMessage(String message, String serverPlayer, HashMap<String, String> serverMonInfo,
                       String clientPlayer, HashMap<String, String> clientMonInfo) {
        super(message);
        this.serverPlayerName = serverPlayer;
        this.clientPlayerName = clientPlayer;
        this.serverMonInfo = serverMonInfo;
        this.clientMonInfo = clientMonInfo;

    }

    public String getServerPlayerName() {return serverPlayerName;}
    public String getClientPlayerName() {return clientPlayerName;}
    public HashMap<String, String> getServerMonInfo() {return serverMonInfo;}
    public HashMap<String, String> getClientMonInfo() {return clientMonInfo;}

    public void setServerPlayerName(String serverPlayerName) {this.serverPlayerName = serverPlayerName;}
    public void setClientPlayerName(String clientPlayerName) {this.clientPlayerName = clientPlayerName;}
    public void setServerMonInfo(HashMap<String, String> serverMonInfo) {this.serverMonInfo = serverMonInfo;}
    public void setClientMonInfo(HashMap<String, String> clientMonInfo) {this.clientMonInfo = clientMonInfo;}
}
