package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.parser.ClassParser;

public class UnmatchedArguments extends Argument
{
    public final ClassParser<?> parser;

    public final Class<?> parameterType;

    public UnmatchedArguments (Field field, ClassParser<?> parser, Class<?> parameterType)
    {
        super(field);
        this.parser = parser;
        this.parameterType = parameterType;
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
