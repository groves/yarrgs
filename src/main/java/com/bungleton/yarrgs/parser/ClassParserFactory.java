//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs.parser;

public interface ClassParserFactory
{
    boolean handles (Class<?> klass);

    Parser<?> createParser (Class<?> klass);
}
