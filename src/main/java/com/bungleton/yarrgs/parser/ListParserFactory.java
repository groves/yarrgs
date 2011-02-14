package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

public class ListParserFactory
    implements FieldParserFactory
{
    public ListParserFactory (ClassParserFactory base)
    {
        _base = base;
    }

    @Override
    public boolean handles (Field f)
    {
        Type t = f.getGenericType();
        return t instanceof ParameterizedType &&
            List.class.isAssignableFrom(f.getType()) &&
            _base.handles((Class<?>)((ParameterizedType)t).getActualTypeArguments()[0]);
    }

    @Override
    public Parser<?> createParser (Field f)
    {
        final Class<?> paramType =
            (Class<?>)((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
        return new Parser<List<?>>() {
            protected final List<Object> vals = new ArrayList<Object>();
            @Override public void add (String arg) {
                Parser<?> internal = _base.createParser(paramType);
                internal.add(arg);
                vals.add(internal.getResult());
            }

            @Override
            public List<?> getResult () {
                return vals;
            }};
    }

    protected final ClassParserFactory _base;
}
