package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public class UnmatchedArguments extends Argument
{
    public UnmatchedArguments (Field field)
    {
        super(field);
    }

    @Override
    public String getBasic ()
    {
        return "[" + field.getName() + "...]";
    }

    @Override
    public String getDetail ()
    {
        return field.getName() + " " + getUsage();
    }

}
