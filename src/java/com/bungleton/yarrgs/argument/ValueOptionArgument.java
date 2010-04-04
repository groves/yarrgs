package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.parser.Parser;

public class ValueOptionArgument extends OptionArgument
{
    public final String placeholder;

    public final Parser<?> parser;

    public ValueOptionArgument (Field field, Parser<?> parser)
    {
        super(field);
        this.placeholder = field.getName().toUpperCase();
        this.parser = parser;
    }

    @Override
    public String getBasic ()
    {
        return shortArg + " " + placeholder;
    }

    @Override
    public String getDetail ()
    {
        return String.format("  %s %s, %s %s %s", shortArg, placeholder, longArg, placeholder,
            getUsage());
    }
}
