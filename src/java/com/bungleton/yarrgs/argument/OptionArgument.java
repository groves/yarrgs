package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

public abstract class OptionArgument extends Argument
{
    public final String shortArg, longArg;

    public OptionArgument (Field field)
    {
        this(field, "-" + field.getName().substring(0, 1), "--" + field.getName());
    }

    public OptionArgument (Field field, String shortArg, String longArg)
    {
        super(field);
        this.shortArg = shortArg;
        this.longArg = longArg;
    }
}
