package aut.pokimin_battlearena.Objects;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;

import aut.pokimin_battlearena.services.DatabaseController;

/**
 * Created by Dom on 13/05/2016.
 */
public class Player implements Serializable {

    private String name;
    private Monster activeMonster;

    DatabaseController dbc;

    //This only creates a player object, inorder to store a initially create a player have to call create method from dbc
    public Player(Context context, String playerName){
        this.name = playerName;
        dbc = new DatabaseController(context);
        activeMonster = new Monster(context, dbc.getActiveMonsterName(name));
    }



    public void printPlayerInfoDebug(){
        Log.v("player", name);
        Log.v("player", activeMonster.getName());

    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public Monster getActiveMonster() {return activeMonster;}
    public void setActiveMonster(Monster monster) {
        this.activeMonster = monster;
        dbc.setActiveMonster(getName(), monster.getName()); }//saves active monster into database}

}
