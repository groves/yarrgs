package com.bungleton.yarrgs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bungleton.yarrgs.argument.Argument;
import com.bungleton.yarrgs.argument.FlagOptionArgument;
import com.bungleton.yarrgs.argument.HelpArgument;
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
        addOption(new HelpArgument());

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
        if (firstOptionalPositional == null) {
            _firstOptionalPositionalIdx = _positionals.size();
        } else {
            _firstOptionalPositionalIdx = _positionals.indexOf(firstOptionalPositional);
        }

        if (unmatchedField == null) {
            _unmatched = null;
            return;
        }
        YarrgConfigurationException.unless(unmatchedField.getGenericType() instanceof ParameterizedType,
            "'" + unmatchedField + "' must specify a type parameter");
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
        throw new YarrgConfigurationException("No ClassParser for type parameter '"
            + unmatchedComponentType.getName() + "' on @Unmatched '" + unmatchedField + "'");
    }

    protected void addOption (OptionArgument parser)
    {
        _orderedOptions.add(parser);
        _shortOptions.put(parser.shortArg.charAt(1), parser);
        _longOptions.put(parser.longArg, parser);
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
        String usage = getUsage();
        List<String> unmatchedArgs = new ArrayList<String>();
        Set<String> specified = new HashSet<String>();
        for (int ii = 0; ii < args.length; ii++) {
            String next = ii + 1 == args.length ? null : args[ii + 1];
            if (args[ii].startsWith("--") && args[ii].length() > 2) {
                if (handleOption(t, args[ii], next, _longOptions.get(args[ii]), specified)) {
                    ii++;
                }
            } else if (args[ii].startsWith("-") && args[ii].length() > 1) {
                // Short options can be chained together off of a single -, but any with another
                // short option following it doesn't have access to the next arg and must be flag
                for (char shortArg : args[ii].substring(1, args[ii].length() - 1).toCharArray()) {
                    handleOption(t, "-" + shortArg, null, _shortOptions.get(shortArg), specified);
                }
                // Let the final short option look at the next arg
                char finalShort = args[ii].charAt(args[ii].length() - 1);
                if (handleOption(t, "-" + finalShort, next, _shortOptions.get(finalShort),
                    specified)) {
                    ii++;
                }
            } else if(positionalsIdx < _positionals.size()) {
                PositionalArgument pos = _positionals.get(positionalsIdx++);
                parse(t, args[ii], pos.parser, pos.field);
            } else {
                unmatchedArgs.add(args[ii]);
            }
        }
        if (positionalsIdx < _firstOptionalPositionalIdx) {
            throw new YarrgParseException(usage, "Required argument '"
                + _positionals.get(positionalsIdx).getShortArgumentDescriptor() + "' missing");
        }
        if (unmatchedArgs.isEmpty()) {
            return t;
        }
        YarrgParseException.unless(_unmatched != null, usage, unmatchedArgs
                + " were given without a corresponding option");
        List<Object> parsed = new ArrayList<Object>(unmatchedArgs.size());
        for (String unparsed : unmatchedArgs) {
            try {
                parsed.add(_unmatched.parser.parse(unparsed, _unmatched.parameterType));
            } catch (RuntimeException e) {
                throw new YarrgParseException(usage, e.getMessage(), e);
            }
        }
        setField(t, _unmatched.field, parsed);
        return t;
    }

    /**
     * Assigns a value inside t for the given OptionArgument extracted by arg.
     *
     * @param nextArg - The argument following this one, or null if there isn't one.
     * @return if nextArg was consumed by handling this option.
     */
    protected boolean handleOption (T t, String arg, String nextArg, OptionArgument parser,
        Set<String> specified)
        throws YarrgParseException
    {
        YarrgParseException.unless(parser != null, getUsage(), "No such option '" + arg + "'");
        YarrgParseException.unless(specified.add(arg), getUsage(), "'" + arg
            + "' specified multiple times");

        if (parser instanceof ValueOptionArgument) {
            YarrgParseException.unless(nextArg != null, getUsage(), "'" + arg
                + "' requires a value following it");
            parse(t, nextArg, ((ValueOptionArgument)parser).parser, parser.field);
            return true;
        } else if (parser instanceof HelpArgument) {
            throw new YarrgHelpException(getUsage(), getDetail());
        } else {
            setField(t, parser.field, true);
            return false;
        }
    }

    /**
     * Sets the <code>f</code> on <code>instance</code> to the value extracted by
     * <code>parser</code> from <code>arg</code>. If <code>parser</code> throws a
     * RuntimeException, it's wrapped in a YarrgParseException and rethrown.
     */
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

    /**
     * Sets <code>f</code> on <code>instance</code> to <code>value</code>.
     * @throws YarrgConfigurationException if setting f throws an exception.
     */
    protected void setField (T instance, Field f, Object value)
    {
        try {
            f.set(instance, value);
        } catch (Exception e) {
            throw new YarrgConfigurationException("Expected to be able to set '" + f + "' to "
                + value, e);
        }
    }

    protected String getUsage ()
    {
        StringBuilder usage = new StringBuilder("Usage: ");
        usage.append(_argumentHolder.getSimpleName()).append(' ');
        if (!_orderedOptions.isEmpty()) {
            usage.append('[');
            for (OptionArgument option : _orderedOptions) {
                usage.append(option.getShortArgumentDescriptor()).append(',');
            }
            usage.setLength(usage.length() - 1);
            usage.append("] ");
        }

        for (PositionalArgument pos : _positionals) {
            usage.append(pos.getShortArgumentDescriptor()).append(' ');
        }
        if (_unmatched != null) {
            usage.append(_unmatched.getShortArgumentDescriptor());
        }
        return usage.toString();
    }

    protected String getDetail ()
    {
        StringBuilder help = new StringBuilder();
        Usage commandUsage = _argumentHolder.getAnnotation(Usage.class);
        if (commandUsage != null) {
            Argument.wrap(help, commandUsage.value(), 2).append("\n\n");
        }
        if (!_orderedOptions.isEmpty()) {
            help.append("Options:\n");
            for (OptionArgument option : _orderedOptions) {
                help.append(option.getDetail()).append('\n');
            }
            help.append('\n');
        }
        if (!_positionals.isEmpty()) {
            help.append("Positionals:\n");
            for (PositionalArgument pos : _positionals) {
                help.append(pos.getDetail()).append('\n');
            }
            help.append('\n');
        }
        if (_unmatched != null && !_unmatched.getUsage().equals("")) {
            help.append("Unmatched:\n");
            help.append(_unmatched.getDetail()).append('\n');
        }
        return help.toString();
    }

    protected final List<Parser<?>> _parsers;
    protected final Class<T> _argumentHolder;
    protected final Map<String, OptionArgument> _longOptions =
        new HashMap<String, OptionArgument>();
    protected final Map<Character, OptionArgument> _shortOptions =
        new HashMap<Character, OptionArgument>();
    protected final List<OptionArgument> _orderedOptions = new ArrayList<OptionArgument>();
    protected final List<PositionalArgument> _positionals = new ArrayList<PositionalArgument>();
    protected final int _firstOptionalPositionalIdx;
    protected final UnmatchedArguments _unmatched;
}
