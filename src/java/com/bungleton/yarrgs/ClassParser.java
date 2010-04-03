package com.bungleton.yarrgs;

import java.lang.reflect.Field;

public abstract class ClassParser<T>
    implements Parser<T>
{
    public ClassParser (Class<T> klass)
    {
        _class = klass;
    }

    public boolean handles (Field f)
    {
        return f.getType().equals(_class);
    }

    protected final Class<T> _class;
}
