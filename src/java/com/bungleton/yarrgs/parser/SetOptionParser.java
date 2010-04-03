package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public class SetOptionParser extends OptionParser
{
    public final String placeholder;

    public SetOptionParser (Field field)
    {
        super(field);
        this.placeholder = field.getName().toUpperCase();
    }

    public Object parse (String arg)
    {
        return arg;
    }

    @Override
    public String getBasic ()
    {
        return shortArg + " " + placeholder;
    }

    @Override
    public String getDetail ()
    {
        return String.format("  %s %s, %s %s %s", shortArg, placeholder, longArg, placeholder,
            getUsage());
    }
}
