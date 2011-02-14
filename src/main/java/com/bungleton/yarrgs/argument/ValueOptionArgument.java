package com.bungleton.yarrgs.argument;

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
    public String getShortArgumentDescriptor ()
    {
        return shortArg + " " + placeholder;
    }

    @Override
    public String getFullArgumentDescriptor ()
    {
        return getShortArgumentDescriptor() + ", " + longArg + " " + placeholder;
    }
}
