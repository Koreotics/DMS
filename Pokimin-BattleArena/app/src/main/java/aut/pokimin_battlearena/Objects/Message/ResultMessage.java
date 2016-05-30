package aut.pokimin_battlearena.Objects.Message;

import android.os.Message;

import aut.pokimin_battlearena.Objects.Player;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public class ResultMessage extends AbstractMessage {

    private String winner;
    private int expGain;

    public ResultMessage(String message, String winner, int expGain){
        super(message);
        this.winner = winner;
        this.expGain = expGain;

    }

    public String getWinner() {return winner;}
    public int getExpGain() {return expGain;}

    public void setWinner(String winner) {this.winner = winner;}
    public void setExpGain(int expGain) {this.expGain = expGain;}
}
