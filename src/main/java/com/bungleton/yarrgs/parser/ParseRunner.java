package com.bungleton.yarrgs.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bungleton.yarrgs.YarrgConfigurationException;
import com.bungleton.yarrgs.YarrgHelpException;
import com.bungleton.yarrgs.YarrgParseException;
import com.bungleton.yarrgs.argument.Argument;
import com.bungleton.yarrgs.argument.HelpArgument;
import com.bungleton.yarrgs.argument.OptionArgument;
import com.bungleton.yarrgs.argument.PositionalArgument;
import com.bungleton.yarrgs.argument.ValueOptionArgument;

/**
 * Holds the state for parsing a particular set of args for a Command, runs that parse, and fills
 * in the values on an instance.
 */
public class ParseRunner<T>
{
    public ParseRunner (String[] args, T instance, Command<T> cmd)
        throws YarrgParseException
    {
        _usage = cmd.getUsage();
        _detail = cmd.getDetail();
        _cmd = cmd;
        List<String> nonFlagged = new ArrayList<String>();
        ARGS: for (int ii = 0; ii < args.length; ii++) {
            String next = ii + 1 == args.length ? null : args[ii + 1];
            if (args[ii].startsWith("--") && args[ii].length() > 2) {
                if (handleOption(args[ii], next, _cmd._longOptions.get(args[ii]))) {
                    ii++;
                }
            } else if (args[ii].startsWith("-") && args[ii].length() > 1) {
                // Short options can be chained together off of a single -, but any with another
                // short option following it doesn't have access to the next arg and must be flag
                char[] shorts = args[ii].substring(1, args[ii].length() - 1).toCharArray();
                for (int jj = 0; jj < shorts.length; jj++) {
                    if (handleOption("-" + shorts[jj], args[ii].substring(2 + jj),
                        _cmd._shortOptions.get(shorts[jj]))) {
                        continue ARGS;
                    }
                }
                // Let the final short option look at the next arg
                char finalShort = args[ii].charAt(args[ii].length() - 1);
                if (handleOption("-" + finalShort, next, _cmd._shortOptions.get(finalShort))) {
                    ii++;
                }
            } else {
                nonFlagged.add(args[ii]);
            }
        }

        int positionalsIdx = 0;
        for (PositionalArgument arg : _cmd._posiPositionals) {
            checkState(nonFlagged.size() > positionalsIdx,
                "Required argument '" + arg.getShortArgumentDescriptor() + "' missing");
            parse(nonFlagged.get(positionalsIdx++), arg);
        }
        if (nonFlagged.size() - positionalsIdx > _cmd._negaPositionals.size()) {
            checkState(_cmd._unmatched != null, "Too many arguments given");
            for (; positionalsIdx < nonFlagged.size() - _cmd._negaPositionals.size(); positionalsIdx++) {
                parse(nonFlagged.get(positionalsIdx), _cmd._unmatched);
            }
        }
        for (PositionalArgument arg : _cmd._negaPositionals) {
            checkState(nonFlagged.size() > positionalsIdx,
                "Required argument '" + arg.getShortArgumentDescriptor() + "' missing");
            parse(nonFlagged.get(positionalsIdx++), arg);
        }
        for (Entry<Argument, Parser<?>> entry : parsers.entrySet()) {
            Object value = entry.getValue().getResult();
            try {
                entry.getKey().field.set(instance, value);
            } catch (Exception e) {
                throw new YarrgConfigurationException("Expected to be able to set '"
                    + entry.getKey().field + "' to " + value, e);
            }
        }
    }

    /**
     * Adds the value of arg and possibly nextArg to the parser for handler.
     *
     * @param nextArg - The argument following this one, or null if there isn't one.
     * @return if nextArg was consumed by handling this option.
     */
    protected boolean handleOption (String arg, String nextArg, OptionArgument handler)
        throws YarrgParseException
    {
        checkState(handler != null, "No such option '" + arg + "'");
        if (handler instanceof ValueOptionArgument) {
            checkState(nextArg != null, "'" + arg + "' requires a value following it");
            parse(nextArg, handler);
            return true;
        } else if (handler instanceof HelpArgument) {
            throw new YarrgHelpException(_usage, _detail);
        } else {
            parse("true", handler);
            return false;
        }
    }

    /**
     * Adds the value from arg to the parser for argDesc.
     */
    protected void parse (String arg, Argument argDesc)
        throws YarrgParseException
    {
        Parser<?> parser = parsers.get(argDesc);
        if (parser == null) {
            parser = _cmd._factory.createParser(argDesc.field);
            parsers.put(argDesc, parser);
        }
        try {
            parser.add(arg);
        } catch (RuntimeException e) {
            throw new YarrgParseException(_usage, e.getMessage(), e);
        }
    }

    protected void checkState (boolean condition, String message)
        throws YarrgParseException
    {
        if (!condition) {
            throw new YarrgParseException(_usage, message);
        }
    }

    protected final String _usage, _detail;
    protected final Command<T> _cmd;
    protected final Map<Argument, Parser<?>> parsers = new HashMap<Argument, Parser<?>>();;
}
