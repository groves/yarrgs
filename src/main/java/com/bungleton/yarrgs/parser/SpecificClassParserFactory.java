//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs.parser;

public abstract class SpecificClassParserFactory
    extends FieldObliviousParserFactory
{
    public SpecificClassParserFactory (Class<?>...klasses)
    {
        _class = klasses;
    }

    public boolean handles (Class<?> klass)
    {
        for (Class<?> ourklass : _class) {
            if (klass.equals(ourklass)) {
                return true;
            }
        }
        return false;
    }

    protected final Class<?>[] _class;
}
