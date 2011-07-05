//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

/**
 * Thrown when a user passes in an invalid value for an argument, doesn't specify a required
 * argument, and so on. The reason for the error is in {@link #getMessage()}.
 */
public class YarrgParseException extends Exception
{
    public YarrgParseException (String usage, String message)
    {
        super(message);
        _usage = usage;
    }

    public YarrgParseException (String usage, String message, Exception cause)
    {
        super(message, cause);
        _usage = usage;
    }

    /**
     * Returns the usage for the command and the error message, suitable for printing on a command
     * failing.
     */
    public String getExitMessage ()
    {
        return _usage + "\n" + getMessage();
    }

    /**
     * Returns the usage for the command.
     */
    public String getUsage()
    {
        return _usage;
    }

    protected final String _usage;
}
