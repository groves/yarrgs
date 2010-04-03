package com.bungleton.yarrgs;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Parsers
{
    public static final Parser<Integer> INT = new ClassParser<Integer>(Integer.TYPE) {
        @Override public Integer parse (String arg, Field f) {
            return Integer.parseInt(arg);
        }
    };

    public static final Parser<Byte> BYTE = new ClassParser<Byte>(Byte.TYPE) {
        @Override public Byte parse (String arg, Field f) {
            return Byte.parseByte(arg);
        }
    };

    public static final Parser<String> STRING = new ClassParser<String>(String.class) {
        @Override public String parse (String arg, Field f) {
            return arg;
        }
    };

    public static final Parser<Date> DATE = new ClassParser<Date>(Date.class) {
        @Override public Date parse (String arg, Field f) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(arg);
            } catch (ParseException e) {
                throw new RuntimeException("'" + arg + "' doesn't match yyyy-MM-dd", e);
            }
        }
    };

    public static final List<Parser<?>> DEFAULT;
    static {
        List<Parser<?>> builder = new ArrayList<Parser<?>>();
        builder.add(INT);
        builder.add(BYTE);
        builder.add(STRING);
        builder.add(DATE);
        DEFAULT = Collections.unmodifiableList(builder);
    }
}
