package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public class FlagParser extends OptionParser
{
    public FlagParser (Field field)
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
        return String.format("  %s ,%-10s %s", shortArg, longArg, getUsage());
    }

}
