package com.bungleton.yarrgs;

public class YarrgHelpException extends YarrgParseException
{
    public YarrgHelpException (String usage, String detail)
    {
        super(usage, detail);
    }
}
