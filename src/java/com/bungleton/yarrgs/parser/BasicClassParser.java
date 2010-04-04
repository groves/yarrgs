package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public abstract class BasicClassParser<T>
    implements ClassParser<T>
{
    public boolean handles (Field f)
    {
        return handles(f.getType());
    }

    public T parse (String arg, Field f)
    {
        return parse(arg, f.getType());
    }
}
