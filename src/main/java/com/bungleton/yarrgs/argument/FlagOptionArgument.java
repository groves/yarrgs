package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

public class FlagOptionArgument extends OptionArgument
{
    public FlagOptionArgument (Field field)
    {
        super(field);
    }

    @Override
    public String getShortArgumentDescriptor ()
    {
        return shortArg;
    }

    @Override
    public String getFullArgumentDescriptor ()
    {
        return getShortArgumentDescriptor() + ", " + longArg;
    }
}
