package aut.pokimin_battlearena.Objects.Message;

import aut.pokimin_battlearena.Objects.Monster;
import aut.pokimin_battlearena.Objects.Player;
import aut.pokimin_battlearena.Objects.Skill;

/**
 * Created by trist on 28/05/2016.
 */
public class SkillMessage extends AbstractMessage {

    private Monster serverMonster;
    private Monster clientMonster;

    private Skill serverSkill;
    private Skill clientSkill;

    public SkillMessage(String message, Monster serverMonster, Skill serverSkill,
                        Monster clientMonster, Skill clientSkill) {
        super(message);

        this.serverMonster = serverMonster;
        this.clientMonster = clientMonster;

        this.serverSkill = serverSkill;
        this.clientSkill = clientSkill;
    }

    public Monster getServerMonster() { return serverMonster; }
    public Monster getClientMonster() { return clientMonster; }
    public Skill   getServerSkill()   { return serverSkill; }
    public Skill   getClientSkill()   { return clientSkill; }

    public void setServerMonster(Monster monster) { serverMonster = monster; }
    public void setClientMonster(Monster monster) { clientMonster = monster; }
    public void setServerSkill(Skill skill)       { serverSkill = skill; }
    public void setClientSkill(Skill skill)       { clientSkill = skill; }
}
