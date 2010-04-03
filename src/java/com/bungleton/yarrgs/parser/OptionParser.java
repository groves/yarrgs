package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.Unparsed;
import com.bungleton.yarrgs.YarrgConfigurationException;

public abstract class OptionParser extends ArgumentParser
{
    public final String shortArg, longArg;

    public OptionParser (Field field)
    {
        super(field);
        this.shortArg = "-" + field.getName().substring(0, 1);
        this.longArg = "--" + field.getName();
    }

    public static OptionParser create (Field f)
    {
        YarrgConfigurationException.unless(f.getAnnotation(Unparsed.class) == null, "'" + f
            + "' is @Unparsed but not a list");
        if (f.getType().equals(Boolean.TYPE)) {
            return new FlagParser(f);
        } else {
            return new SetOptionParser(f);
        }
    }

    public static boolean handles (Field f)
    {
        return f.getType().equals(Boolean.TYPE) || f.getType().equals(String.class);
    }
}
