package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public abstract class FieldObliviousParserFactory
    implements ClassParserFactory, FieldParserFactory
{
    @Override
    public boolean handles (Field f)
    {
        return handles(f.getType());
    }

    @Override
    public Parser<?> createParser (Field f)
    {
        return createParser(f.getType());
    }
}
