package com.bungleton.yarrgs.parser;

public abstract class SpecificClassParserFactory
    extends FieldObliviousParserFactory
{
    public SpecificClassParserFactory (Class<?> klass)
    {
        _class = klass;
    }

    public boolean handles (Class<?> klass)
    {
        return klass.equals(_class);
    }

    protected final Class<?> _class;
}
