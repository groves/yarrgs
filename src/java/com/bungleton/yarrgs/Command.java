package com.bungleton.yarrgs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bungleton.yarrgs.argument.FlagOptionArgument;
import com.bungleton.yarrgs.argument.OptionArgument;
import com.bungleton.yarrgs.argument.PositionalArgument;
import com.bungleton.yarrgs.argument.UnmatchedArguments;
import com.bungleton.yarrgs.argument.ValueOptionArgument;
import com.bungleton.yarrgs.parser.ClassParser;
import com.bungleton.yarrgs.parser.Parser;

public class Command<T>
{
    public Command (Class<T> argumentHolder, List<Parser<?>> parsers)
    {
        _argumentHolder = argumentHolder;
        _parsers = parsers;
        Map<Integer, PositionalArgument> positionals = new HashMap<Integer, PositionalArgument>();
        Field unmatchedField = null;
        for (Field f : argumentHolder.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            Positional pos = f.getAnnotation(Positional.class);
            if (f.getAnnotation(Unmatched.class) != null) {
                YarrgConfigurationException.unless(pos == null, "'" + f
                    + "' has @Unmatched and @Positional");
                YarrgConfigurationException.unless(f.getType().equals(List.class),
                    "'" + f + "' is @Unmatched but not a List");
                YarrgConfigurationException.unless(unmatchedField == null,
                    "'" + f + "' and '" + unmatchedField + "' both have @Unmatched");
                unmatchedField = f;
                continue;
            } else if (f.getType().equals(Boolean.TYPE)) {
                addOption(new FlagOptionArgument(f));
                continue;
            }
            Parser<?> parser = null;
            for (Parser<?> p : _parsers) {
                if (p.handles(f)) {
                    parser = p;
                    break;
                }
            }
            YarrgConfigurationException.unless(parser != null, "Unhandled type: " + f);
            if (pos != null && parser != null) {
                PositionalArgument existent =
                    positionals.put(pos.position(), new PositionalArgument(f, parser));
                if (existent != null) {
                    throw new YarrgConfigurationException("Attempted to assign '" + f
                        + "' to the same position as '" + existent.field + "'");
                }
            } else {
                addOption(new ValueOptionArgument(f, parser));
            }
        }

        Field firstOptionalPositional = null;
        for (int ii = 0; ii < positionals.size(); ii++) {
            YarrgConfigurationException.unless(positionals.containsKey(ii + 1),
                "There were positionals past " + (ii + 1) + ", but none for it");
            PositionalArgument p = positionals.get(ii + 1);
            YarrgConfigurationException.unless(p.optional || firstOptionalPositional == null,
                "Non-optional positional argument '" + p.field
                + "' can't come after optional positional argument '"+ firstOptionalPositional
                + "'");
            if (p.optional && firstOptionalPositional == null) {
                firstOptionalPositional = p.field;
            }
            _positionals.add(p);
        }

        if (unmatchedField == null) {
            _unmatched = null;
            return;
        }
        YarrgConfigurationException.unless(unmatchedField.getGenericType() instanceof ParameterizedType,
            "'" + unmatchedField + "' must specify its type parameter");
        Class<?> unmatchedComponentType =
            (Class<?>)((ParameterizedType)unmatchedField.getGenericType()).getActualTypeArguments()[0];
        for (Parser<?> potential : parsers) {
            if (potential instanceof ClassParser<?> &&
                    ((ClassParser<?>)potential).handles(unmatchedComponentType)) {
                _unmatched = new UnmatchedArguments(unmatchedField, (ClassParser<?>)potential,
                    unmatchedComponentType);
                return;
            }
        }
        throw new YarrgConfigurationException("No ClassParser for type in @Unmatched: "
            + unmatchedField);
    }

    protected void addOption (OptionArgument parser)
    {
        _orderedOptions.add(parser);
        _options.put(parser.shortArg, parser);
        _options.put(parser.longArg, parser);
    }

    public T parse (String[] args)
        throws YarrgParseException
    {
        T t;
        try {
            t = _argumentHolder.newInstance();
        } catch (Exception e) {
            throw new YarrgConfigurationException("'" + _argumentHolder
                + "' must have a public no-arg constructor", e);
        }
        int positionalsIdx = 0;
        List<String> unmatchedArgs = new ArrayList<String>();
        for (int ii = 0; ii < args.length; ii++) {
            if (args[ii].equals("-h") || args[ii].equals("--help")) {
                throw new YarrgHelpException(getUsage(), getDetail());
            } else if (_options.containsKey(args[ii])) {
                OptionArgument parser = _options.get(args[ii]);
                if (parser instanceof ValueOptionArgument) {
                    parse(t, args[++ii], ((ValueOptionArgument)parser).parser, parser.field);
                } else {
                    setField(t, parser.field, true);
                }
            } else if(positionalsIdx < _positionals.size()) {
                PositionalArgument pos = _positionals.get(positionalsIdx++);
                parse(t, args[ii], pos.parser, pos.field);
            } else {
                unmatchedArgs.add(args[ii]);
            }
        }
        if (_unmatched != null) {
            List<Object> parsed = new ArrayList<Object>(unmatchedArgs.size());
            for (String unparsed : unmatchedArgs) {
                try {
                    parsed.add(_unmatched.parser.parse(unparsed, _unmatched.parameterType));
                } catch (RuntimeException e) {
                    throw new YarrgParseException(getUsage(), e.getMessage(), e);
                }
            }
            setField(t, _unmatched.field, parsed);
        } else if (!unmatchedArgs.isEmpty()) {
            throw new YarrgParseException(getUsage(),
                unmatchedArgs + " were given without a corresponding option");
        }
        return t;
    }

    protected void parse (T instance, String arg, Parser<?> parser, Field f)
        throws YarrgParseException
    {
        Object result;
        try {
            result = parser.parse(arg, f);
        } catch (RuntimeException e) {
            throw new YarrgParseException(getUsage(), e.getMessage(), e);
        }
        setField(instance, f, result);
    }

    protected String getUsage ()
    {
        StringBuilder usage = new StringBuilder("Usage: ");
        usage.append(_argumentHolder.getSimpleName()).append(' ');
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

    protected void setField (T instance, Field f, Object value)
    {
        try {
            f.set(instance, value);
        } catch (Exception e) {
            throw new YarrgConfigurationException("Expected to be able to set '" + f + "' to "
                + value, e);
        }
    }

    protected final List<Parser<?>> _parsers;
    protected final Class<T> _argumentHolder;
    protected final Map<String, OptionArgument> _options = new HashMap<String, OptionArgument>();
    protected final List<OptionArgument> _orderedOptions = new ArrayList<OptionArgument>();
    protected final List<PositionalArgument> _positionals = new ArrayList<PositionalArgument>();
    protected final UnmatchedArguments _unmatched;
}