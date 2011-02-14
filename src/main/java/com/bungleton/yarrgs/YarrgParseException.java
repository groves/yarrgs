package com.bungleton.yarrgs;

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

    public static void unless (boolean condition, String usage, String message)
        throws YarrgParseException
    {
        if (!condition) {
            throw new YarrgParseException(usage, message);
        }
    }

    public String getExitMessage ()
    {
        return _usage + "\n" + getMessage();
    }

    public String getUsage()
    {
        return _usage;
    }

    protected final String _usage;
}
