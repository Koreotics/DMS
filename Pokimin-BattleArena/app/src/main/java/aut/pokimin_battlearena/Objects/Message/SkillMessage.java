package aut.pokimin_battlearena.Objects.Message;

import aut.pokimin_battlearena.Objects.HashMap<String, String>;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;

/**
 * Created by trist on 28/05/2016.
 */
public class SkillMessage extends AbstractMessage {

    private HashMap<String, String> serverMonster;
    private HashMap<String, String> clientMonster;

    private Skill serverSkill;
    private Skill clientSkill;

    public SkillMessage(String message, HashMap<String, String> serverMonster, Skill serverSkill,
                        HashMap<String, String> clientMonster, Skill clientSkill) {
        super(message);

        this.serverMonster = serverMonster;
        this.clientMonster = clientMonster;

        this.serverSkill = serverSkill;
        this.clientSkill = clientSkill;
    }

    public HashMap<String, String> getServerMonster() { return serverMonster; }
    public HashMap<String, String> getClientMonster() { return clientMonster; }
    public Skill   getServerSkill()   { return serverSkill; }
    public Skill   getClientSkill()   { return clientSkill; }

    public void setServerMonster(HashMap<String, String> monster) { serverMonster = monster; }
    public void setClientMonster(HashMap<String, String> monster) { clientMonster = monster; }
    public void setServerSkill(Skill skill)       { serverSkill = skill; }
    public void setClientSkill(Skill skill)       { clientSkill = skill; }
}
