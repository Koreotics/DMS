package aut.pokimin_battlearena.Objects.Message;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class BattleMessage extends AbstractMessage {

    private Monster serverMonster;
    private Monster clientMonster;

    public BattleMessage(String message, Monster serverMonster, Monster clientMonster) {
        super(message);
        this.serverMonster = serverMonster;
        this.clientMonster = clientMonster;
    }

    public Monster getClientMonster() {return clientMonster;}
    public Monster getServerMonster() {return serverMonster;}

    public void setClientMonster(Monster clientMonster) {this.clientMonster = clientMonster;}
    public void setServerMonster(Monster serverMonster) {this.serverMonster = serverMonster;}
}
