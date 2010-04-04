package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

public abstract class OptionArgument extends Argument
{
    public final String shortArg, longArg;

    public OptionArgument (Field field)
    {
        super(field);
        this.shortArg = "-" + field.getName().substring(0, 1);
        this.longArg = "--" + field.getName();
    }
}
