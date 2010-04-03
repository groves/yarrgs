package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bungleton.yarrgs.Parser;
import com.bungleton.yarrgs.Positional;
import com.bungleton.yarrgs.Unmatched;
import com.bungleton.yarrgs.YarrgConfigurationException;
import com.bungleton.yarrgs.YarrgHelpException;
import com.bungleton.yarrgs.YarrgParseException;

public class Command
{

    public Command (Object destination, Map<Class<?>, Parser<?>> parsers)
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
            Parser<?> parser = parsers.get(f.getType());
            Positional pos = f.getAnnotation(Positional.class);
            if (pos != null && parser != null) {
                YarrgConfigurationException.unless(un == null, "'" + f
                    + "' is @Unparsed and @Positional");
                Field existent = positionals.put(pos.position(), f);
                YarrgConfigurationException.unless(existent == null, "Attempted to assign '" + f
                    + "' to the same position as '" + existent + "'");
            } else if (un != null) {
                YarrgConfigurationException.unless(f.getType().equals(List.class),
                    "'" + f + "' is @Unparsed but not a list");
                YarrgConfigurationException.unless(unmatchedField == null,
                    "'" + f + "' and '" + unmatchedField + "' both have @Unparsed");
                unmatchedField = f;
            } else if (f.getType().equals(Boolean.TYPE)) {
                addOption(new FlagOptionArgument(f));
            } else if(parser != null) {
                addOption(new ValueOptionArgument(f, parser));
            } else {
                throw new YarrgConfigurationException("Field '" + f + "' with unhandled type");
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
            _positionals.add(new PositionalArgument(f, parsers.get(f.getType())));
        }

        _unmatched = unmatchedField == null ? null : new UnmatchedArguments(unmatchedField);
    }

    protected void addOption (OptionArgument parser)
    {
        _orderedOptions.add(parser);
        _options.put(parser.shortArg, parser);
        _options.put(parser.longArg, parser);
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
                    setField(parser.field, parse(args[++ii], ((ValueOptionArgument)parser).parser));
                } else {
                    setField(parser.field, true);
                }
            } else if(positionalsIdx < _positionals.size()) {
                PositionalArgument pos = _positionals.get(positionalsIdx++);
                setField(pos.field, parse(args[ii], pos.parser));
            } else {
                unmatchedArgs.add(args[ii]);
            }
        }
        if (_unmatched != null) {
            setField(_unmatched.field, unmatchedArgs);
        } else if (!unmatchedArgs.isEmpty()) {
            throw new YarrgParseException(getUsage(),
                unmatchedArgs + " were given without a corresponding option");
        }
    }

    protected Object parse (String arg, Parser<?> parser)
        throws YarrgParseException
    {
        try {
            return parser.parse(arg);
        } catch (RuntimeException e) {
            throw new YarrgParseException(getUsage(), e.getMessage(), e);
        }
    }

    protected String getUsage ()
    {
        StringBuilder usage = new StringBuilder("Usage: ");
        usage.append(_destination.getClass().getSimpleName()).append(' ');
        if (!_orderedOptions.isEmpty()) {
            usage.append('[');
            for (OptionArgument option : _orderedOptions) {
                usage.append(option.getBasic()).append('|');
            }
            usage.setLength(usage.length() - 1);
            usage.append("] ");
        }
        for (PositionalArgument pos : _positionals) {
            usage.append(pos.getBasic()).append(' ');
        }
        if (_unmatched != null) {
            usage.append(_unmatched.getBasic());
        }
        return usage.toString();
    }

    protected String getDetail ()
    {
        StringBuilder help = new StringBuilder();
        for (OptionArgument option : _orderedOptions) {
            help.append(option.getDetail()).append('\n');
        }
        for (PositionalArgument pos : _positionals) {
            help.append(pos.getDetail()).append('\n');
        }
        if (_unmatched != null && !_unmatched.getUsage().equals("")) {
            help.append(_unmatched.getDetail()).append('\n');
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
    protected final List<OptionArgument> _orderedOptions = new ArrayList<OptionArgument>();
    protected final List<PositionalArgument> _positionals = new ArrayList<PositionalArgument>();
    protected final UnmatchedArguments _unmatched;
}
