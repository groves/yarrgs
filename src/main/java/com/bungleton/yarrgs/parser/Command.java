package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bungleton.yarrgs.Positional;
import com.bungleton.yarrgs.Unmatched;
import com.bungleton.yarrgs.Usage;
import com.bungleton.yarrgs.YarrgConfigurationException;
import com.bungleton.yarrgs.YarrgHelpException;
import com.bungleton.yarrgs.YarrgParseException;
import com.bungleton.yarrgs.argument.Argument;
import com.bungleton.yarrgs.argument.FlagOptionArgument;
import com.bungleton.yarrgs.argument.HelpArgument;
import com.bungleton.yarrgs.argument.OptionArgument;
import com.bungleton.yarrgs.argument.PositionalArgument;
import com.bungleton.yarrgs.argument.UnmatchedArguments;
import com.bungleton.yarrgs.argument.ValueOptionArgument;

public class Command<T>
{
    public Command (Class<T> argumentHolder, FieldParserFactory factory)
    {
        _argumentHolder = argumentHolder;
        _factory = factory;
        Map<Integer, PositionalArgument> posiPositionals = new HashMap<Integer, PositionalArgument>();
        Map<Integer, PositionalArgument> negaPositionals = new HashMap<Integer, PositionalArgument>();
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
            YarrgConfigurationException.unless(factory.handles(f), "Unhandled type: " + f);
            if (pos != null) {
                Map<Integer, PositionalArgument> positionals =
                        pos.value() < 0 ? negaPositionals : posiPositionals;
                PositionalArgument existent =
                    positionals.put(pos.value(), new PositionalArgument(f));
                if (existent != null) {
                    throw new YarrgConfigurationException("Attempted to assign '" + f
                        + "' to the same position as '" + existent.field + "'");
                }
            } else {
                addOption(new ValueOptionArgument(f));
            }
        }
        addOption(new HelpArgument());

        for (int ii = 0; ii < posiPositionals.size(); ii++) {
            YarrgConfigurationException.unless(posiPositionals.containsKey(ii),
                "There were positionals past " + ii + ", but none for it");
            _posiPositionals.add(posiPositionals.get(ii));
        }

        for (int ii = -negaPositionals.size(); ii < 0; ii++) {
            YarrgConfigurationException.unless(negaPositionals.containsKey(ii),
                "There were positionals past " + ii + ", but none for it");
            _negaPositionals.add(negaPositionals.get(ii));
        }

        if (unmatchedField == null) {
            _unmatched = null;
            return;
        }
        YarrgConfigurationException.unless(factory.handles(unmatchedField), "'" + unmatchedField
            + "' must specify a type parameter");
        _unmatched = new UnmatchedArguments(unmatchedField);
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
        String usage = getUsage();
        Map<Argument, Parser<?>> parsers = new HashMap<Argument, Parser<?>>();
        List<String> nonFlagged = new ArrayList<String>();
        ARGS: for (int ii = 0; ii < args.length; ii++) {
            String next = ii + 1 == args.length ? null : args[ii + 1];
            if (args[ii].startsWith("--") && args[ii].length() > 2) {
                if (handleOption(t, args[ii], next, _longOptions.get(args[ii]), parsers)) {
                    ii++;
                }
            } else if (args[ii].startsWith("-") && args[ii].length() > 1) {
                // Short options can be chained together off of a single -, but any with another
                // short option following it doesn't have access to the next arg and must be flag
                char[] shorts = args[ii].substring(1, args[ii].length() - 1).toCharArray();
                for (int jj = 0; jj < shorts.length; jj++) {
                    if (handleOption(t, "-" + shorts[jj], args[ii].substring(2 + jj),
                        _shortOptions.get(shorts[jj]), parsers)) {
                        continue ARGS;
                    }
                }
                // Let the final short option look at the next arg
                char finalShort = args[ii].charAt(args[ii].length() - 1);
                if (handleOption(t, "-" + finalShort, next, _shortOptions.get(finalShort),
                    parsers)) {
                    ii++;
                }
            } else {
                nonFlagged.add(args[ii]);
            }
        }

        int positionalsIdx = 0;
        for (PositionalArgument arg : _posiPositionals) {
            YarrgParseException.unless(nonFlagged.size() > positionalsIdx, usage,
                "Required argument '" + arg.getShortArgumentDescriptor() + "' missing");
            parse(nonFlagged.get(positionalsIdx++), createParser(arg, parsers));
        }
        if (nonFlagged.size() - positionalsIdx > _negaPositionals.size()) {
            YarrgParseException.unless(_unmatched != null, usage, "Too many arguments given");
            for (; positionalsIdx < nonFlagged.size() - _negaPositionals.size(); positionalsIdx++) {
                parse(nonFlagged.get(positionalsIdx), createParser(_unmatched, parsers));
            }
        }
        for (PositionalArgument arg : _negaPositionals) {
            YarrgParseException.unless(nonFlagged.size() > positionalsIdx, usage,
                "Required argument '" + arg.getShortArgumentDescriptor() + "' missing");
            parse(nonFlagged.get(positionalsIdx++), createParser(arg, parsers));
        }
        for (Entry<Argument, Parser<?>> entry : parsers.entrySet()) {
            Object value = entry.getValue().getResult();
            try {
                entry.getKey().field.set(t, value);
            } catch (Exception e) {
                throw new YarrgConfigurationException("Expected to be able to set '"
                    + entry.getKey().field + "' to " + value, e);
            }
        }
        return t;
    }

    protected Parser<?> createParser (Argument arg, Map<Argument, Parser<?>> parsers)
    {
        Parser<?> parser = parsers.get(arg);
        if (parser == null) {
            parser = _factory.createParser(arg.field);
            parsers.put(arg, parser);
        }
        return parser;
    }

    /**
     * Assigns a value inside t for the given OptionArgument extracted by arg.
     *
     * @param nextArg - The argument following this one, or null if there isn't one.
     * @return if nextArg was consumed by handling this option.
     */
    protected boolean handleOption (T t, String arg, String nextArg, OptionArgument handler,
        Map<Argument, Parser<?>> parsers)
        throws YarrgParseException
    {
        YarrgParseException.unless(handler != null, getUsage(), "No such option '" + arg + "'");
        if (handler instanceof ValueOptionArgument) {
            YarrgParseException.unless(nextArg != null, getUsage(), "'" + arg
                + "' requires a value following it");
            parse(nextArg, createParser(handler, parsers));
            return true;
        } else if (handler instanceof HelpArgument) {
            throw new YarrgHelpException(getUsage(), getDetail());
        } else {
            parse("true", createParser(handler, parsers));
            return false;
        }
    }

    /**
     * Sets the <code>f</code> on <code>instance</code> to the value extracted by
     * <code>parser</code> from <code>arg</code>. If <code>parser</code> throws a
     * RuntimeException, it's wrapped in a YarrgParseException and rethrown.
     */
    protected void parse (String arg, Parser<?> parser)
        throws YarrgParseException
    {
        try {
            parser.add(arg);
        } catch (RuntimeException e) {
            throw new YarrgParseException(getUsage(), e.getMessage(), e);
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

        for (PositionalArgument pos : _posiPositionals) {
            usage.append(pos.getShortArgumentDescriptor()).append(' ');
        }
        if (_unmatched != null) {
            usage.append(_unmatched.getShortArgumentDescriptor()).append(' ');
        }
        for (PositionalArgument pos : _negaPositionals) {
            usage.append(pos.getShortArgumentDescriptor()).append(' ');
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
        if (!_posiPositionals.isEmpty() || !_negaPositionals.isEmpty()) {
            help.append("Positionals:\n");
            for (PositionalArgument pos : _posiPositionals) {
                help.append(pos.getDetail()).append('\n');
            }
            for (PositionalArgument pos : _negaPositionals) {
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

    protected final FieldParserFactory _factory;
    protected final Class<T> _argumentHolder;
    protected final Map<String, OptionArgument> _longOptions =
        new HashMap<String, OptionArgument>();
    protected final Map<Character, OptionArgument> _shortOptions =
        new HashMap<Character, OptionArgument>();
    protected final List<OptionArgument> _orderedOptions = new ArrayList<OptionArgument>();
    protected final List<PositionalArgument> _posiPositionals = new ArrayList<PositionalArgument>();
    protected final List<PositionalArgument> _negaPositionals = new ArrayList<PositionalArgument>();
    protected final UnmatchedArguments _unmatched;
}
