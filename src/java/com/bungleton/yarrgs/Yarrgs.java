package com.bungleton.yarrgs;

import java.util.List;


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
        return new Command<T>(argsType, parsers).parse(args);
    }
}
