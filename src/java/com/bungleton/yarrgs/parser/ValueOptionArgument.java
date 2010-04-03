package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public class ValueOptionArgument extends OptionArgument
{
    public final String placeholder;

    public ValueOptionArgument (Field field)
    {
        super(field);
        this.placeholder = field.getName().toUpperCase();
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
