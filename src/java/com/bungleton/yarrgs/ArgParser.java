package com.bungleton.yarrgs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgParser
{
    public ArgParser (Object parseDestination)
    {
        _destination = parseDestination;
        StringBuilder basic = new StringBuilder("Usage: ");
        basic.append(_destination.getClass().getSimpleName()).append(" [");
        Map<Integer, Field> positionals = new HashMap<Integer, Field>();
        Class<?> parseType = parseDestination.getClass();
        Field unparsed = null;
        Formatter detailed = new Formatter();
        for (Field f : parseType.getFields()) {
            Unparsed un = f.getAnnotation(Unparsed.class);
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            } else if (f.getType().equals(Boolean.TYPE)) {
                basic.append(fillInArguments(_flags, f, detailed)).append('|');
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
                    basic.append(fillInArguments(_args, f, detailed));
                    basic.append(' ').append(f.getName().toUpperCase()).append('|');
                }
            } else if (un != null) {
                YarrgConfigurationException.unless(f.getType().equals(List.class),
                    "'" + f + "' is @Unparsed but not a list");
                unparsed = f;
            } else {
                throw new YarrgConfigurationException("Field '" + f + "' with unknown type");
            }
        }
        basic.setLength(basic.length() - 1);
        if (!_flags.isEmpty() || !_args.isEmpty()) {
            basic.append(']');
        }

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
            basic.append(' ');
            if (pos.optional()) {
                basic.append('[');
            }
            basic.append(f.getName());
            if (pos.optional()) {
                basic.append(']');
            }
        }

        _unparsed = unparsed;
        if (_unparsed != null) {
            basic.append(" [").append(_unparsed.getName()).append("...]");
        }
        _usage = basic.append('\n').toString();
        _help = _usage + detailed;
    }

    public void parse (String[] args)
        throws YarrgParseException
    {
        int positionalsIdx = 0;
        List<String> unmatched = new ArrayList<String>();
        for (int ii = 0; ii < args.length; ii++) {
            if (args[ii].equals("-h") || args[ii].equals("--help")) {
                throw new YarrgHelpException(getHelp());
            } else if (_flags.containsKey(args[ii])) {
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
            throw new YarrgParseException(getUsage() + unmatched
                + " were given without a corresponding option");
        }
    }

    protected String getHelp ()
    {
        return _help;
    }

    protected String getUsage ()
    {
        return _usage;
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

    protected String fillInArguments (Map<String, Field> argHolder, Field f, Formatter detailed)
    {
        String shortArg ="-" + f.getName().substring(0, 1);
        String longArg ="--" + f.getName();
        argHolder.put(shortArg, f);
        argHolder.put(longArg, f);
        Usage u = f.getAnnotation(Usage.class);
        detailed.format("  %s ,%-10s %s\n", shortArg, longArg, u == null ? "" : u.value());
        return shortArg;
    }

    protected final String _usage, _help;
    protected final Object _destination;
    protected final Map<String, Field> _flags = new HashMap<String, Field>();
    protected final Map<String, Field> _args = new HashMap<String, Field>();
    protected final List<Field> _positional = new ArrayList<Field>();
    protected final Field _unparsed;
}
