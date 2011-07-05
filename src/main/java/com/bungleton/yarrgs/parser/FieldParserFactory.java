//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

public interface FieldParserFactory
{
    boolean handles(Field f);

    Parser<?> createParser(Field f);
}
