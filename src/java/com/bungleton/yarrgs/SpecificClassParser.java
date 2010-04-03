package com.bungleton.yarrgs;

public abstract class SpecificClassParser<T> extends BasicClassParser<T>
{
    public SpecificClassParser (Class<T> klass)
    {
        _class = klass;
    }

    public boolean handles (Class<?> klass)
    {
        return klass.equals(_class);
    }

    protected final Class<T> _class;
}
