package aut.pokimin_battlearena.Objects.Message;

import java.util.HashMap;



import java.util.HashMap;


import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;

/**
 * Created by trist on 28/05/2016.
 */
public class SkillMessage extends AbstractMessage {

    private static final long serialVersionUID = 1;

    private HashMap<String, String> serverMonster;
    private HashMap<String, String> clientMonster;

    private HashMap<String, String> serverSkill;
    private HashMap<String, String> clientSkill;

    public SkillMessage(String message, HashMap<String, String> serverMonster, HashMap<String, String> serverSkill,
                        HashMap<String, String> clientMonster, HashMap<String, String> clientSkill) {
        super(message);

        this.serverMonster = serverMonster;
        this.clientMonster = clientMonster;

        this.serverSkill = serverSkill;
        this.clientSkill = clientSkill;
    }


    public HashMap<String, String> getServerMonster() { return serverMonster; }
    public HashMap<String, String> getClientMonster() { return clientMonster; }
    public HashMap<String, String>    getServerSkill()   { return serverSkill; }
    public HashMap<String, String>    getClientSkill()   { return clientSkill; }

    public void setServerMonster(HashMap<String, String> monster) { serverMonster = monster; }
    public void setClientMonster(HashMap<String, String> monster) { clientMonster = monster; }
    public void setServerSkill(HashMap<String, String>  skill)       { serverSkill = skill; }
    public void setClientSkill(HashMap<String, String>  skill)       { clientSkill = skill; }

}
