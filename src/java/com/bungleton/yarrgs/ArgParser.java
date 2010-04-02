package com.bungleton.yarrgs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.HashMap;
import java.util.Map;

public class ArgParser
{

    protected ArgParser (Object parseDestination)
    {
        _destination = parseDestination;
        Class<?> parseType = parseDestination.getClass();
        for (Field f : parseType.getFields()) {
            if (!Modifier.isPublic(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) {
                continue;
            } else if (f.getType().equals(Boolean.TYPE)) {
                fillInArguments(_flags, f);
            } else if (f.getType().equals(String.class)) {
                fillInArguments(_args, f);
            } else {
                throw new RuntimeException("Field '" + f + "' with unknown type");
            }
        }
    }

    protected void parse (String[] args)
    {
        for (int ii = 0; ii < args.length; ii++) {
            if (_flags.containsKey(args[ii])) {
                setField(_flags.get(args[ii]), true);
            } else if (_args.containsKey(args[ii])) {
                setField(_args.get(args[ii]), args[++ii]);
            }
        }
    }

    protected void setField (Field f, Object value)
    {
        try {
            f.set(_destination, value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected void fillInArguments (Map<String, Field> argHolder, Field f)
    {
        argHolder.put("-" + f.getName().substring(0, 1), f);
        argHolder.put("--" + f.getName(), f);
    }

    protected final Object _destination;
    protected Map<String, Field> _flags = new HashMap<String, Field>();
    protected Map<String, Field> _args = new HashMap<String, Field>();
}
