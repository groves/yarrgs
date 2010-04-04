package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

public class FlagOptionArgument extends OptionArgument
{
    public FlagOptionArgument (Field field)
    {
        super(field);
    }

    @Override
    public String getBasic ()
    {
        return shortArg;
    }

    @Override
    public String getDetail ()
    {
        return String.format("  %s, %-10s %s", shortArg, longArg, getUsage());
    }

}
