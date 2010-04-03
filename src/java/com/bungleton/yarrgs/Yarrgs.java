package com.bungleton.yarrgs;

import java.util.List;

import com.bungleton.yarrgs.parser.Command;

public class Yarrgs
{
    public static <T> T parseInMain (Class<T> argsType, String[] args)
    {
        return parseInMain(argsType, args, Parsers.DEFAULT);
    }

    public static <T> T parseInMain (Class<T> argsType, String[] args, List<Parser<?>> parsers)
    {
        try {
            return parse(argsType, args, parsers);
        } catch (YarrgParseException e) {
            System.err.println(e.getExitMessage());
            System.exit(1);
            throw new IllegalStateException("Java continued past a System.exit call");
        }
    }

    public static <T> T parse (Class<T> argsType, String[] args)
        throws YarrgParseException
    {
        return parse(argsType, args, Parsers.DEFAULT);

    }

    public static <T> T parse (Class<T> argsType, String[] args, List<Parser<?>> parsers)
        throws YarrgParseException
    {
        T t;
        try {
            t = argsType.newInstance();
        } catch (Exception e) {
            throw new YarrgConfigurationException("'" + argsType
                + "' must have a public no-arg constructor", e);
        }
        new Command(t, parsers).parse(args);
        return t;
    }
}
