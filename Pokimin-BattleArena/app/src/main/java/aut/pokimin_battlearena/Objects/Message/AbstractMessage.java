package aut.pokimin_battlearena.Objects.Message;

import java.io.Serializable;

/**
 * @author Tristan Borja (1322097)
 * @author Dominic Yuen  (1324837)
 * @author Gierdino Julian Santoso (15894898)
 */
public abstract class AbstractMessage implements Serializable{

    private static final long serialVersionUID = 1;
    public String message;

    public AbstractMessage(String message){

        this.message = message;

    }

    public String getMessage() { return message; }
}
