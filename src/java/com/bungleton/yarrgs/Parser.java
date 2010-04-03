package com.bungleton.yarrgs;

import java.lang.reflect.Field;

public interface Parser<T>
{
    boolean handles(Field f);

    T parse(String arg, Field f);
}
