package aut.pokimin_battlearena.Objects;

import android.content.Context;
import android.util.Log;

import aut.pokimin_battlearena.services.DatabaseController;

/**
 * Created by Dom on 13/05/2016.
 */
public class Skill {

    private String name = "";
    private String type = "";
    private double multiply = 0;
    private int maxPP = 0;
    private int speed = 0;

    DatabaseController dbc;

    public Skill(Context context, String name){
        this.name = name;
        dbc = new DatabaseController(context);
        dbc.setSkillInfo(name, this);
    }

    public void printSkillInfo(){
        Log.v(name.toString(), type);
        Log.v(name.toString(), multiply+"");
        Log.v(name.toString(), maxPP+"");
        Log.v(name.toString(), speed+"");
    }

    //----------------------------------------------------------------------------------------------
    //Getters and Setters
    //----------------------------------------------------------------------------------------------

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public double getMultiply() {return multiply;}
    public void setMultiply(double multiply) {this.multiply = multiply;}
    public int getMaxPP() {return maxPP;}
    public void reducePP() {this.maxPP = maxPP - 1;}
    public void setMaxPP(int pp) {this.maxPP = pp;}
    public int getSpeed() {return speed;}
    public void setSpeed(int speed) {this.speed = speed;}
}
