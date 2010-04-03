package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.Unmatched;
import com.bungleton.yarrgs.YarrgConfigurationException;

public abstract class OptionArgument extends Argument
{
    public final String shortArg, longArg;

    public OptionArgument (Field field)
    {
        super(field);
        this.shortArg = "-" + field.getName().substring(0, 1);
        this.longArg = "--" + field.getName();
    }

    public static OptionArgument create (Field f)
    {
        YarrgConfigurationException.unless(f.getAnnotation(Unmatched.class) == null, "'" + f
            + "' is @Unparsed but not a list");
        if (f.getType().equals(Boolean.TYPE)) {
            return new FlagOptionArgument(f);
        } else {
            return new ValueOptionArgument(f);
        }
    }

    public static boolean handles (Field f)
    {
        return f.getType().equals(Boolean.TYPE) || f.getType().equals(String.class);
    }
}
