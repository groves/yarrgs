package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public interface FieldParserFactory
{
    boolean handles(Field f);

    Parser<?> createParser(Field f);
}
