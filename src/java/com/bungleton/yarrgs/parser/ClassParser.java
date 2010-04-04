package com.bungleton.yarrgs.parser;

public interface ClassParser<T>
    extends Parser<T>
{
    boolean handles (Class<?> klass);

    T parse(String arg, Class<?> klass);
}
