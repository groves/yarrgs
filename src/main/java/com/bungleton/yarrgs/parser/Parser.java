package com.bungleton.yarrgs.parser;

public interface Parser<T>
{
    void add (String arg);

    T getResult ();
}
