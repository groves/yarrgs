package com.bungleton.yarrgs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgParser
{
    public ArgParser (Object parseDestination)
    {
        _destination = parseDestination;
        Map<Integer, Field> positionals = new HashMap<Integer, Field>();
        Class<?> parseType = parseDestination.getClass();
        for (Field f : parseType.getFields()) {
            if (!Modifier.isPublic(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) {
                continue;
            } else if (f.getType().equals(Boolean.TYPE)) {
                fillInArguments(_flags, f);
            } else if (f.getType().equals(String.class)) {
                Positional pos = f.getAnnotation(Positional.class);
                if (pos != null) {
                    Field existent = positionals.put(pos.position(), f);
                    if (existent != null) {
                        throw new RuntimeException("Attempted to assign '" + f
                            + "' to the same position as '" + existent + "'");
                    }
                } else {
                    fillInArguments(_args, f);
                }
            } else {
                throw new RuntimeException("Field '" + f + "' with unknown type");
            }
        }
        for (int ii = 0; ii < positionals.size(); ii++) {
            if (!positionals.containsKey(ii + 1)) {
                throw new RuntimeException("There were positionals past " + (ii + 1)
                    + ", but none for it");
            }
            _positional.add(positionals.get(ii + 1));
        }
    }

    public void parse (String[] args)
    {
        int positionalsIdx = 0;
        for (int ii = 0; ii < args.length; ii++) {
            if (_flags.containsKey(args[ii])) {
                setField(_flags.get(args[ii]), true);
            } else if (_args.containsKey(args[ii])) {
                setField(_args.get(args[ii]), args[++ii]);
            } else if(positionalsIdx < _positional.size()) {
                Field posField = _positional.get(positionalsIdx++);
                setField(posField, args[ii]);
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
    protected List<Field> _positional = new ArrayList<Field>();
}
