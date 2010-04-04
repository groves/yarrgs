package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.Usage;

public abstract class Argument
{
    public final Field field;

    public Argument (Field field)
    {
        this.field = field;
    }

    public String getUsage ()
    {
        Usage u = field.getAnnotation(Usage.class);
        return u == null ? "" : u.value();
    }

    public abstract String getBasic ();

    public abstract String getDetail ();
}
