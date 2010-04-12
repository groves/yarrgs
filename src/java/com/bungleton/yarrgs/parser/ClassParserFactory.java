package com.bungleton.yarrgs.parser;

public interface ClassParserFactory
{
    boolean handles (Class<?> klass);

    Parser<?> createParser (Class<?> klass);
}
