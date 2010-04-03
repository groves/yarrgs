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
                throw new YarrgHelpException(getHelp(false));
            } else if (_options.containsKey(args[ii])) {
                OptionParser parser = _options.get(args[ii]);
                if (parser instanceof SetOptionParser) {
                    setField(parser.field, ((SetOptionParser)parser).parse(args[++ii]));
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
            throw new YarrgParseException(getHelp(true) + unmatched
                + " were given without a corresponding option");
        }
    }

    protected String getHelp (boolean oneLine)
    {
        StringBuilder basic = new StringBuilder("Usage: ");
        StringBuilder detail = new StringBuilder();
        basic.append(_destination.getClass().getSimpleName()).append(' ');
        if (!options.isEmpty()) {
            basic.append('[');
            for (OptionParser option : options) {
                basic.append(option.getBasic()).append('|');
                detail.append(option.getDetail()).append('\n');
            }
            basic.setLength(basic.length() - 1);
            basic.append("] ");
        }
        for (PositionalParser pos : positonals) {
            basic.append(pos.getBasic()).append(' ');
            detail.append(pos.getDetail()).append('\n');
        }
        if (unparsed != null) {
            basic.append(unparsed.getBasic());
            if (!unparsed.getUsage().equals("")) {
                detail.append(unparsed.getDetail()).append('\n');
            }
        }
        String usage = basic.append('\n').toString();
        return oneLine ? usage : usage + detail;
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
