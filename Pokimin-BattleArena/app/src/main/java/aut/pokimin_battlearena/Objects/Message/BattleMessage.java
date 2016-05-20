package aut.pokimin_battlearena.Objects.Message;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;

/**
 * Created by Dom on 20/05/2016.
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

    public void setClientMonster(Monster clientMonster) {this.clientMonster = clientMonster;}
    public Monster getServerMonster() {return serverMonster;}
    public void setServerMonster(Monster serverMonster) {this.serverMonster = serverMonster;}
}
