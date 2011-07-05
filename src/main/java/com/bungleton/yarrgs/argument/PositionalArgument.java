//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

public class PositionalArgument extends Argument
{
    public PositionalArgument (Field field)
    {
        super(field);
    }

    @Override
    public String getShortArgumentDescriptor ()
    {
        return field.getName();
    }
}
