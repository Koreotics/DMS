package aut.pokimin_battlearena.Objects.Message;

import android.os.Message;

import aut.pokimin_battlearena.Objects.Player;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class ResultMessage extends AbstractMessage {

    private Player winner;
    private int expGain;

    public ResultMessage(String message, Player winner, int expGain){
        super(message);
        this.winner = winner;
        this.expGain = expGain;

    }

    public Player getWinner() {return winner;}
    public int getExpGain() {return expGain;}

    public void setWinner(Player winner) {this.winner = winner;}
    public void setExpGain(int expGain) {this.expGain = expGain;}
}
