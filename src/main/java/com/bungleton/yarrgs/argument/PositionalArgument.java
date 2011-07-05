package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

public class PositionalArgument extends Argument
{
    public PositionalArgument (Field field)
    {
        super(field);
    }

    @Override
    public String getShortArgumentDescriptor ()
    {
        return field.getName();
    }
}
