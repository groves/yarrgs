package com.bungleton.yarrgs;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Parsers
{
    public static final Parser<Integer> INT = new Parser<Integer>() {
        @Override public Integer parse (String arg) {
            return Integer.parseInt(arg);
        }
    };

    public static final Parser<Byte> BYTE = new Parser<Byte>() {
        @Override public Byte parse (String arg) {
            return Byte.parseByte(arg);
        }
    };

    public static final Parser<String> STRING = new Parser<String>() {
        @Override public String parse (String arg) {
            return arg;
        }
    };

    public static final Parser<Date> DATE = new Parser<Date>() {
        @Override public Date parse (String arg) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(arg);
            } catch (ParseException e) {
                throw new RuntimeException("'" + arg + "' doesn't match yyyy-MM-dd");
            }
        }
    };

    public static final Map<Class<?>, Parser<?>> DEFAULT;
    static {
        Map<Class<?>, Parser<?>> builder = new HashMap<Class<?>, Parser<?>>();
        builder.put(Integer.TYPE, INT);
        builder.put(Byte.TYPE, BYTE);
        builder.put(String.class, STRING);
        builder.put(Date.class, DATE);
        DEFAULT = Collections.unmodifiableMap(builder);
    }
}
