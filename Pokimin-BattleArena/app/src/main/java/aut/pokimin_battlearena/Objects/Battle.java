package aut.pokimin_battlearena.Objects;

import aut.pokimin_battlearena.R;

/**
 * Created by Dom on 20/05/2016.
 *
 * Holds battle logic
 */
public class Battle {

    private Monster serverMonster;
    private Monster clientMonster;

    private boolean fasterMonsterProtected = false;
    private boolean slowerMonsterProtect = false;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // CONSTRUCTOR
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public Battle(Monster serverMonster, Monster clientMonster){

        this.serverMonster = serverMonster;
        this.clientMonster = clientMonster;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Getters and Setters
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public Monster getServerMonster() {return serverMonster;}
    public void setServerMonster(Monster serverMonster) {this.serverMonster = serverMonster;}
    public Monster getClientMonster() {return clientMonster;}
    public void setClientMonster(Monster clientMonster) {this.clientMonster = clientMonster;}



    public void determineFasterMonster(Skill serverSkill, Skill clientSkill){

        //TODO add conditions for when the monsters ahve same speed

        int speedServ = serverSkill.getSpeed() + getServerMonster().getSpeed();
        int speedCli = clientSkill.getSpeed() + getClientMonster().getSpeed();

        if(speedServ > speedCli){
            calculateMoveResults(getServerMonster(), serverSkill, getClientMonster(), clientSkill);
        }else{calculateMoveResults(getClientMonster(), clientSkill, getServerMonster(), serverSkill);}


    }

    public void calculateMoveResults(Monster fasterMonster, Skill fastSkill,
                                      Monster slowMonster, Skill slowSkill){

        boolean monsterDead = false;
        //faster monster uses skill first
        switch(fastSkill.getType()){
            case "protect": setProtect(true);
                break;
            case "damage": monsterDead = dealDamage(fasterMonster, fastSkill, slowMonster);
                break;
            case "defence": increaseDefence(fasterMonster, fastSkill);
                break;
        }

        if(!monsterDead) {
            //slower monster uses skill
            switch (slowSkill.getType()) {
                case "protect":
                    setProtect(true);
                    break;
                case "damage":
                    dealDamage(fasterMonster, fastSkill, slowMonster);
                    break;
                case "defence":
                    increaseDefence(fasterMonster, fastSkill);
                    break;
            }
        }

        if(monsterDead){
            //TODO do something, add winner method 
        }

    }


    //deals damage to defending monster using attack monster attack and skills attack
    public boolean dealDamage(Monster attackingMon, Skill attack, Monster defendingMon){

        //assumes if there is a protected monster that it will be the faster monster
        if(!fasterMonsterProtected) {
            //calculates damage and reduces the defending monsters health
            int totalAttack = attackingMon.getAttack() + (int) attack.getMultiply();
            int newHealth = defendingMon.getHealth() - (totalAttack - (int) (defendingMon.getDefence() * totalAttack));
            defendingMon.setHealth(newHealth);
            if (defendingMon.getHealth() <= 0) //returns true if monster is killed
                return true;
        }

        //reduces monster attack skill PP
        if(attack.getName().equals(attackingMon.getSkill1().getName())){attackingMon.getSkill1().reducePP();}
        else if(attack.getName().equals(attackingMon.getSkill2().getName())){attackingMon.getSkill2().reducePP(); }
        else if(attack.getName().equals(attackingMon.getSkill3().getName())){attackingMon.getSkill3().reducePP(); }
        else if(attack.getName().equals(attackingMon.getSkill4().getName())){attackingMon.getSkill4().reducePP(); }

        return false; //returns false if monster didnt die
    }

    public void increaseDefence(Monster monster, Skill skill){
        monster.setDefence(monster.getDefence() + skill.getMultiply());
    }

    public void setProtect(boolean fastMonster){

        if(fastMonster)fasterMonsterProtected = true;
        else{ slowerMonsterProtect = true;}
    }
}
