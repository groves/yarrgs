package com.bungleton.yarrgs.argument;

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
    public String getShortArgumentDescriptor ()
    {
        return optional ? "[" + field.getName() + "]" : field.getName();
    }
}
