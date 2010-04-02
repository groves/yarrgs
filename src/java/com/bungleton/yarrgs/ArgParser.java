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
        Field unparsed = null;
        for (Field f : parseType.getFields()) {
            Unparsed un = f.getAnnotation(Unparsed.class);
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            } else if (f.getType().equals(Boolean.TYPE)) {
                fillInArguments(_flags, f);
            } else if (f.getType().equals(String.class)) {
                Positional pos = f.getAnnotation(Positional.class);
                YarrgConfigurationException.unless(un == null,
                    "'" + f + "' is @Unparsed but not a list");
                if (pos != null) {
                    Field existent = positionals.put(pos.position(), f);
                    YarrgConfigurationException.unless(existent == null,
                        "Attempted to assign '" + f + "' to the same position as '" +
                        existent + "'");
                } else {
                    fillInArguments(_args, f);
                }
            } else if (un != null) {
                YarrgConfigurationException.unless(f.getType().equals(List.class),
                    "'" + f + "' is @Unparsed but not a list");
                unparsed = f;
            } else {
                throw new YarrgConfigurationException("Field '" + f + "' with unknown type");
            }
        }
        _unparsed = unparsed;
        Field firstOptionalPositional = null;
        for (int ii = 0; ii < positionals.size(); ii++) {
            YarrgConfigurationException.unless(positionals.containsKey(ii + 1),
                "There were positionals past " + (ii + 1) + ", but none for it");
            Field f = positionals.get(ii + 1);
            Positional pos = f.getAnnotation(Positional.class);
            YarrgConfigurationException.unless(pos.optional() || firstOptionalPositional == null,
                "Non-optional positional argument '" + f
                + "' can't come after optional positional argument '"+ firstOptionalPositional
                + "'");
            if (pos.optional() && firstOptionalPositional == null) {
                firstOptionalPositional = f;
            }
            _positional.add(f);
        }
    }

    public void parse (String[] args)
    {
        int positionalsIdx = 0;
        List<String> unmatched = new ArrayList<String>();
        for (int ii = 0; ii < args.length; ii++) {
            if (_flags.containsKey(args[ii])) {
                setField(_flags.get(args[ii]), true);
            } else if (_args.containsKey(args[ii])) {
                setField(_args.get(args[ii]), args[++ii]);
            } else if(positionalsIdx < _positional.size()) {
                Field posField = _positional.get(positionalsIdx++);
                setField(posField, args[ii]);
            } else {
                unmatched.add(args[ii]);
            }
        }
        if (_unparsed != null) {
            setField(_unparsed, unmatched);
        } else if (!unmatched.isEmpty()) {
            throw new RuntimeException(unmatched + " were given without a corresponding option");
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
    protected final Map<String, Field> _flags = new HashMap<String, Field>();
    protected final Map<String, Field> _args = new HashMap<String, Field>();
    protected final List<Field> _positional = new ArrayList<Field>();
    protected final Field _unparsed;
}
