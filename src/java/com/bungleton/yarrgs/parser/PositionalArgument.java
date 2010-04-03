package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.Positional;

public class PositionalArgument extends Argument
{
    public final boolean optional;

    public PositionalArgument (Field field)
    {
        super(field);
        optional = field.getAnnotation(Positional.class).optional();
    }

    @Override
    public String getBasic ()
    {
        return optional ? "[" + field.getName() + "]" : field.getName();
    }

    @Override
    public String getDetail ()
    {
        return field.getName() + " " + getUsage();
    }
}
