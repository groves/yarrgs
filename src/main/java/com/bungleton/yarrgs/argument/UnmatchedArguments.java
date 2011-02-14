package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

public class UnmatchedArguments extends Argument
{
    public UnmatchedArguments (Field field)
    {
        super(field);
    }

    @Override
    public String getShortArgumentDescriptor ()
    {
        return "[" + field.getName() + "...]";
    }
}
