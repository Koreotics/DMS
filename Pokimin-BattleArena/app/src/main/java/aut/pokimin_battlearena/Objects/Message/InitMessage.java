package aut.pokimin_battlearena.Objects.Message;

import java.io.Serializable;

import aut.pokimin_battlearena.Objects.Player;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class InitMessage extends AbstractMessage {

    private Player serverPlayer;
    private Player clientPlayer;

    public InitMessage(String message, Player serverPlayer, Player clientPlayer) {
        super(message);
        this.serverPlayer = serverPlayer;
        this.clientPlayer = clientPlayer;
    }

    public Player getServerPlayer() {return serverPlayer;}
    public void setServerPlayer(Player serverPlayer) {this.serverPlayer = serverPlayer;}

    public Player getClientPlayer() {return clientPlayer;}
    public void setClientPlayer(Player clientPlayer) {this.clientPlayer = clientPlayer;}
}
