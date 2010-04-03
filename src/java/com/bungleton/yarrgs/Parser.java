package com.bungleton.yarrgs;

public interface Parser<T>
{
    T parse(String arg);
}
