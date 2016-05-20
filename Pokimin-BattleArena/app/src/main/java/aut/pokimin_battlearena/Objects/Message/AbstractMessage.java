package aut.pokimin_battlearena.Objects.Message;

import java.io.Serializable;

/**
 * Created by Dom on 20/05/2016.
 */
public abstract class AbstractMessage implements Serializable{

    public String message;

    public AbstractMessage(String message){

        this.message = message;

    }
}
