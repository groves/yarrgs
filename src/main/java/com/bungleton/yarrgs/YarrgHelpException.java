//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

/**
 * Thrown when a user requests help with <code>-h</code> or <code>--help</code>.
 *
 * @see YarrgParseException How to present this error
 */
public class YarrgHelpException extends YarrgParseException
{
    public YarrgHelpException (String usage, String detail)
    {
        super(usage, detail);
    }
}
