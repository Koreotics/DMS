package aut.pokimin_battlearena.Objects;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;

import aut.pokimin_battlearena.services.DatabaseController;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class Monster implements Serializable {

    private String name = "";
    private String img = "";
    private String element = "";
    private int level = 1;
    private int exp = 0;
    private int health = 0;
    private double defence = 0;
    private int attack = 0;
    private int speed = 0;
    private Skill skill1 = null;
    private Skill skill2 = null;
    private Skill skill3 = null;
    private Skill skill4 = null;

    public DatabaseController dbc;

    public Monster(Context context, String name){
        dbc = new DatabaseController(context);
        this.name = name;
        dbc.setMonsterStandardInfo(context, name, this);
        dbc.setMonsterCurrentStats(name, this);

    }

    //saves the stats of the Current instance of Monster into the databse
    public void saveMonsterCurrentInfo(){dbc.setMonsterCurrentStats(this.name, this);}



    public void printInfo(){
        Log.v("monsterInfo", name);
        Log.v("monsterInfo", element);
        Log.v("monsterInfo", level+"");
        Log.v("monsterInfo", exp+"");
        Log.v("monsterInfo", health+"");
        Log.v("monsterInfo", defence+"");
        Log.v("monsterInfo", attack+"");
        Log.v("monsterInfo", speed+"");
        skill1.printSkillInfo();
        skill2.printSkillInfo();
        skill3.printSkillInfo();
        skill4.printSkillInfo();
    }

    //----------------------------------------------------------------------------------------------
    //Getters and Setters
    //----------------------------------------------------------------------------------------------

    public Skill getSkill4() {return skill4;}
    public Skill getSkill3() {return skill3;}
    public Skill getSkill2() {return skill2;}
    public Skill getSkill1() {return skill1;}
    public int getSpeed() {return speed;}
    public int getAttack() {return attack;}
    public double getDefence() {return defence;}
    public int getHealth() {return health;}
    public int getExp() {return exp;}
    public String getElement() {return element;}
    public int getLevel() {return level;}
    public String getImg() {return img;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public void setImg(String img) {this.img = img;}
    public void setElement(String element) {this.element = element;}
    public void setLevel(int level) {this.level = level;}
    public void setExp(int exp) {this.exp = exp;}
    public void setHealth(int health) {this.health = health;}
    public void setDefence(double defence) {this.defence = defence;}
    public void setAttack(int attack) {this.attack = attack;}
    public void setSpeed(int speed) {this.speed = speed;}
    public void setSkill1(Skill skill1) {this.skill1 = skill1;}
    public void setSkill2(Skill skill2) {this.skill2 = skill2;}
    public void setSkill3(Skill skill3) {this.skill3 = skill3;}
    public void setSkill4(Skill skill4) {this.skill4 = skill4;}
}
