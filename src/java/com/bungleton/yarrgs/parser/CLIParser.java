package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bungleton.yarrgs.Positional;
import com.bungleton.yarrgs.Unparsed;
import com.bungleton.yarrgs.YarrgConfigurationException;
import com.bungleton.yarrgs.YarrgHelpException;
import com.bungleton.yarrgs.YarrgParseException;

public class CLIParser
{
    public final List<OptionParser> options = new ArrayList<OptionParser>();
    public final List<PositionalParser> positonals = new ArrayList<PositionalParser>();
    public final UnparsedParser unparsed;

    public CLIParser (Object parseDestination)
    {
        _destination = parseDestination;
        Map<Integer, Field> positionals = new HashMap<Integer, Field>();
        Class<?> parseType = parseDestination.getClass();
        Field unparsedF = null;
        for (Field f : parseType.getFields()) {
            Unparsed un = f.getAnnotation(Unparsed.class);
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            Positional pos = f.getAnnotation(Positional.class);
            if (pos != null) {
                YarrgConfigurationException.unless(un == null, "'" + f
                    + "' is @Unparsed and @Positional");
                Field existent = positionals.put(pos.position(), f);
                YarrgConfigurationException.unless(existent == null, "Attempted to assign '" + f
                    + "' to the same position as '" + existent + "'");
            } else if (OptionParser.handles(f)) {
                OptionParser parser = OptionParser.create(f);
                options.add(parser);
                _options.put(parser.shortArg, parser);
                _options.put(parser.longArg, parser);
            } else if (un != null) {
                YarrgConfigurationException.unless(f.getType().equals(List.class),
                    "'" + f + "' is @Unparsed but not a list");
                YarrgConfigurationException.unless(unparsedF == null,
                    "'" + f + "' and '" + unparsedF + "' both have @Unparsed");
                unparsedF = f;
            } else {
                throw new YarrgConfigurationException("Field '" + f + "' with unknown type");
            }
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
        }

        unparsed = unparsedF == null ? null : new UnparsedParser(unparsedF);
    }

    public void parse (String[] args)
        throws YarrgParseException
    {
        int positionalsIdx = 0;
        List<String> unmatched = new ArrayList<String>();
        for (int ii = 0; ii < args.length; ii++) {
            if (args[ii].equals("-h") || args[ii].equals("--help")) {
                throw new YarrgHelpException(getUsage(), getDetail());
            } else if (_options.containsKey(args[ii])) {
                OptionParser parser = _options.get(args[ii]);
                if (parser instanceof SetOptionParser) {
                    setField(parser.field, args[++ii]);
                } else {
                    setField(parser.field, true);
                }
            } else if(positionalsIdx < _positional.size()) {
                Field posField = _positional.get(positionalsIdx++);
                setField(posField, args[ii]);
            } else {
                unmatched.add(args[ii]);
            }
        }
        if (unparsed != null) {
            setField(unparsed.field, unmatched);
        } else if (!unmatched.isEmpty()) {
            throw new YarrgParseException(getUsage(),
                unmatched + " were given without a corresponding option");
        }
    }

    protected String getUsage ()
    {
        StringBuilder usage = new StringBuilder("Usage: ");
        usage.append(_destination.getClass().getSimpleName()).append(' ');
        if (!options.isEmpty()) {
            usage.append('[');
            for (OptionParser option : options) {
                usage.append(option.getBasic()).append('|');
            }
            usage.setLength(usage.length() - 1);
            usage.append("] ");
        }
        for (PositionalParser pos : positonals) {
            usage.append(pos.getBasic()).append(' ');
        }
        if (unparsed != null) {
            usage.append(unparsed.getBasic());
        }
        return usage.toString();
    }

    protected String getDetail ()
    {
        StringBuilder help = new StringBuilder();
        for (OptionParser option : options) {
            help.append(option.getDetail()).append('\n');
        }
        for (PositionalParser pos : positonals) {
            help.append(pos.getDetail()).append('\n');
        }
        if (unparsed != null && !unparsed.getUsage().equals("")) {
            help.append(unparsed.getDetail()).append('\n');
        }
        return help.toString();
    }

    protected void setField (Field f, Object value)
    {
        try {
            f.set(_destination, value);
        } catch (Exception e) {
            throw new YarrgConfigurationException("Expected to be able to set '" + f + "' to "
                + value, e);
        }
    }

    protected final Object _destination;
    protected final Map<String, OptionParser> _options = new HashMap<String, OptionParser>();
    protected final List<Field> _positional = new ArrayList<Field>();
}
