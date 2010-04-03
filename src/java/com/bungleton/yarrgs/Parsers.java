package com.bungleton.yarrgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Parsers
{
    public static final Parser<Integer> INT = new SpecificClassParser<Integer>(Integer.TYPE) {
        @Override public Integer parse (String arg, Class<?> f) {
            return Integer.parseInt(arg);
        }
    };

    public static final Parser<Byte> BYTE = new SpecificClassParser<Byte>(Byte.TYPE) {
        @Override public Byte parse (String arg, Class<?> f) {
            return Byte.parseByte(arg);
        }
    };

    public static final Parser<String> STRING = new SpecificClassParser<String>(String.class) {
        @Override public String parse (String arg, Class<?> f) {
            return arg;
        }
    };

    public static final Parser<Date> DATE = new SpecificClassParser<Date>(Date.class) {
        @Override public Date parse (String arg, Class<?> f) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(arg);
            } catch (ParseException e) {
                throw new RuntimeException("'" + arg + "' doesn't match yyyy-MM-dd", e);
            }
        }
    };

    public static final Parser<Object> ENUM = new BasicClassParser<Object>() {
        @Override public boolean handles (Class<?> klass) {
            return klass.isEnum();
        }

        @Override public Object parse (String arg, Class<?> klass) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumType = (Class<? extends Enum>)klass;
            try {
                @SuppressWarnings("unchecked")
                Object val = Enum.valueOf(enumType, arg);
                return val;
            } catch (IllegalArgumentException ex) {
                StringBuilder error = new StringBuilder("Expecting one of ");
                for (Object option : klass.getEnumConstants()) {
                    error.append(((Enum<?>)option).name()).append('|');
                }
                error.setLength(error.length() - 1);
                error.append(", not '" + arg + "'");
                throw new IllegalArgumentException(error.toString(), ex);
            }
        }
    };

    public static final List<Parser<?>> DEFAULT;
    static {
        List<Parser<?>> builder = new ArrayList<Parser<?>>();
        builder.add(INT);
        builder.add(BYTE);
        builder.add(STRING);
        builder.add(ENUM);
        builder.add(DATE);
        DEFAULT = Collections.unmodifiableList(builder);
    }
}
