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

    public String getExitMessage ()
    {
        return _usage + "\n\n" + getMessage();
    }

    public String getUsage()
    {
        return _usage;
    }

    protected final String _usage;
}
