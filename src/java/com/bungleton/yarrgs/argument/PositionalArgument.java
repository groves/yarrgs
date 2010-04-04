package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.Positional;
import com.bungleton.yarrgs.parser.Parser;

public class PositionalArgument extends Argument
{
    public final boolean optional;

    public final Parser<?> parser;

    public PositionalArgument (Field field, Parser<?> parser)
    {
        super(field);
        optional = field.getAnnotation(Positional.class).optional();
        this.parser = parser;
    }

    @Override
    public String getShortArgumentDescriptor ()
    {
        return optional ? "[" + field.getName() + "]" : field.getName();
    }
}
