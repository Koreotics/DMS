package aut.pokimin_battlearena.Objects.Message;

import java.util.HashMap;


import aut.pokimin_battlearena.Objects.Player;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BattleMessage extends AbstractMessage {

    private static final long serialVersionUID = 1;
    private HashMap<String, String>  serverMonster;
    private HashMap<String, String>  clientMonster;

    public BattleMessage(String message, HashMap<String, String> serverMonster, HashMap<String, String>  clientMonster) {
        super(message);
        this.serverMonster = serverMonster;
        this.clientMonster = clientMonster;
    }

    public HashMap<String, String>  getClientMonster() {return clientMonster;}
    public HashMap<String, String>  getServerMonster() {return serverMonster;}

    public void setClientMonster(HashMap<String, String>  clientMonster) {this.clientMonster = clientMonster;}
    public void setServerMonster(HashMap<String, String>  serverMonster) {this.serverMonster = serverMonster;}
}
