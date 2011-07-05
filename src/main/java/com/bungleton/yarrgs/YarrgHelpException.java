package com.bungleton.yarrgs;

/**
 * Thrown when a user requests help with <code>-h</code> or <code>--help</code>.
 *
 * @see {@link YarrgParseException} - for how to present this error.
 */
public class YarrgHelpException extends YarrgParseException
{
    public YarrgHelpException (String usage, String detail)
    {
        super(usage, detail);
    }
}
