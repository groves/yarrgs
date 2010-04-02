package com.bungleton.yarrgs;

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
