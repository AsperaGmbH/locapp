package de.aspera.locapp.cmd;

/**
 * This standard Command interface is used to define a run method for all
 * command classes.
 *
 * @author Daniel.Weiss
 *
 */
public interface CommandRunnable {
    public static final String EMPTY_VALUE = "";

    void run();
}
