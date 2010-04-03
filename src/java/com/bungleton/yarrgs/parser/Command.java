package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bungleton.yarrgs.Positional;
import com.bungleton.yarrgs.Unmatched;
import com.bungleton.yarrgs.YarrgConfigurationException;
import com.bungleton.yarrgs.YarrgHelpException;
import com.bungleton.yarrgs.YarrgParseException;

public class Command
{
    public final List<OptionArgument> options = new ArrayList<OptionArgument>();
    public final List<PositionalArgument> positonals = new ArrayList<PositionalArgument>();
    public final UnmatchedArguments unmatched;

    public Command (Object destination)
    {
        _destination = destination;
        Map<Integer, Field> positionals = new HashMap<Integer, Field>();
        Class<?> parseType = destination.getClass();
        Field unmatchedField = null;
        for (Field f : parseType.getFields()) {
            Unmatched un = f.getAnnotation(Unmatched.class);
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
            } else if (OptionArgument.handles(f)) {
                OptionArgument parser = OptionArgument.create(f);
                options.add(parser);
                _options.put(parser.shortArg, parser);
                _options.put(parser.longArg, parser);
            } else if (un != null) {
                YarrgConfigurationException.unless(f.getType().equals(List.class),
                    "'" + f + "' is @Unparsed but not a list");
                YarrgConfigurationException.unless(unmatchedField == null,
                    "'" + f + "' and '" + unmatchedField + "' both have @Unparsed");
                unmatchedField = f;
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

        unmatched = unmatchedField == null ? null : new UnmatchedArguments(unmatchedField);
    }

    public void parse (String[] args)
        throws YarrgParseException
    {
        int positionalsIdx = 0;
        List<String> unmatchedArgs = new ArrayList<String>();
        for (int ii = 0; ii < args.length; ii++) {
            if (args[ii].equals("-h") || args[ii].equals("--help")) {
                throw new YarrgHelpException(getUsage(), getDetail());
            } else if (_options.containsKey(args[ii])) {
                OptionArgument parser = _options.get(args[ii]);
                if (parser instanceof ValueOptionArgument) {
                    setField(parser.field, args[++ii]);
                } else {
                    setField(parser.field, true);
                }
            } else if(positionalsIdx < _positional.size()) {
                Field posField = _positional.get(positionalsIdx++);
                setField(posField, args[ii]);
            } else {
                unmatchedArgs.add(args[ii]);
            }
        }
        if (unmatched != null) {
            setField(unmatched.field, unmatchedArgs);
        } else if (!unmatchedArgs.isEmpty()) {
            throw new YarrgParseException(getUsage(),
                unmatchedArgs + " were given without a corresponding option");
        }
    }

    protected String getUsage ()
    {
        StringBuilder usage = new StringBuilder("Usage: ");
        usage.append(_destination.getClass().getSimpleName()).append(' ');
        if (!options.isEmpty()) {
            usage.append('[');
            for (OptionArgument option : options) {
                usage.append(option.getBasic()).append('|');
            }
            usage.setLength(usage.length() - 1);
            usage.append("] ");
        }
        for (PositionalArgument pos : positonals) {
            usage.append(pos.getBasic()).append(' ');
        }
        if (unmatched != null) {
            usage.append(unmatched.getBasic());
        }
        return usage.toString();
    }

    protected String getDetail ()
    {
        StringBuilder help = new StringBuilder();
        for (OptionArgument option : options) {
            help.append(option.getDetail()).append('\n');
        }
        for (PositionalArgument pos : positonals) {
            help.append(pos.getDetail()).append('\n');
        }
        if (unmatched != null && !unmatched.getUsage().equals("")) {
            help.append(unmatched.getDetail()).append('\n');
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
    protected final Map<String, OptionArgument> _options = new HashMap<String, OptionArgument>();
    protected final List<Field> _positional = new ArrayList<Field>();
}
