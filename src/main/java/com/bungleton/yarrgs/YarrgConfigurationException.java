//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

/**
 * Thrown when a argument class is misconfigured eg having two options with the same flag.
 */
public class YarrgConfigurationException extends RuntimeException
{
    public YarrgConfigurationException (String msg)
    {
        super(msg);
    }

    public YarrgConfigurationException (String msg, Exception cause)
    {
        super(msg, cause);
    }

    public static void unless (boolean condition, String msg)
    {
        if (!condition) {
            throw new YarrgConfigurationException(msg);
        }
    }

}
