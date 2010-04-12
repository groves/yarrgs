package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;

import java.util.Date;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Parsers
{

    public static FieldParserFactory createFieldParserFactory ()
    {
        return createFieldParserFactory(composeClassParserFactory(DEFAULTS));
    }

    public static FieldParserFactory createFieldParserFactory (ClassParserFactory classFactory)
    {
        return composeFieldParserFactory(composeFieldParserFactory(DEFAULTS),
            new ListParserFactory(classFactory));
    }

    public static ClassParserFactory composeClassParserFactory (
        final ClassParserFactory... factories)
    {
        return new ClassParserFactory() {
            @Override public boolean handles (Class<?> klass) {
                return findHandler(klass) != null;
            }

            @Override public Parser<?> createParser (Class<?> klass) {
                return findHandler(klass).createParser(klass);
            }

            protected ClassParserFactory findHandler(Class<?> klass) {
                for (ClassParserFactory factory : factories) {
                    if (factory.handles(klass)) {
                        return factory;
                    }
                }
                return null;
            }
        };
    }

    public static FieldParserFactory composeFieldParserFactory (
        final FieldParserFactory ...factories)
    {
        return new FieldParserFactory() {
            @Override public boolean handles (Field f) {
                return findHandler(f) != null;
            }

            @Override public Parser<?> createParser (Field f) {
                return findHandler(f).createParser(f);
            }

            protected FieldParserFactory findHandler(Field f) {
                for (FieldParserFactory factory : factories) {
                    if (factory.handles(f)) {
                        return factory;
                    }
                }
                return null;
            }
        };
    }

    public static final FieldObliviousParserFactory INT = new FixedClassParserFactory<Integer>(Integer.TYPE) {
        @Override public Integer parse (String arg) {
            return Integer.parseInt(arg);
        }
    };

    public static final FieldObliviousParserFactory BYTE = new FixedClassParserFactory<Byte>(Byte.TYPE) {
        @Override public Byte parse (String arg) {
            return Byte.parseByte(arg);
        }
    };

    public static final FieldObliviousParserFactory BOOLEAN = new FixedClassParserFactory<Boolean>(Boolean.TYPE) {
        @Override public Boolean parse (String arg) {
            if (arg.equalsIgnoreCase("true")) {
                return true;
            } else if(arg.equalsIgnoreCase("false")) {
                return false;
            } else {
                throw new RuntimeException("'true' + or 'false' expected");
            }

        }
    };

    public static final FieldObliviousParserFactory STRING = new FixedClassParserFactory<String>(String.class) {
        @Override public String parse (String arg) {
            return arg;
        }
    };

    public static final FieldObliviousParserFactory DATE = new FixedClassParserFactory<Date>(Date.class) {
        @Override public Date parse (String arg) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(arg);
            } catch (ParseException e) {
                throw new RuntimeException("'" + arg + "' doesn't match yyyy-MM-dd", e);
            }
        }
    };

    public static final FieldObliviousParserFactory FILE = new FixedClassParserFactory<File>(File.class) {
        @Override public File parse (String arg) {
            return new File(arg);
        }
    };

    public static final FieldObliviousParserFactory ENUM = new FieldObliviousParserFactory() {
        @Override public boolean handles (Class<?> klass) {
            return klass.isEnum();
        }

        @Override public Parser<?> createParser (final Class<?> klass) {
            return new SingleArgParser<Object>() {
                @Override public Object parse (String arg) {
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
                }};
        }
    };

    protected static final FieldObliviousParserFactory[] DEFAULTS =
        { INT, BYTE, BOOLEAN, STRING, ENUM, DATE, FILE };
}
