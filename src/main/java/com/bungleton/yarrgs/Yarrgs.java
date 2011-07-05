package com.bungleton.yarrgs;

import com.bungleton.yarrgs.parser.Command;
import com.bungleton.yarrgs.parser.FieldParserFactory;
import com.bungleton.yarrgs.parser.Parsers;

public class Yarrgs
{
    public static <T> T parseInMain (Class<T> argsType, String[] args)
    {
        return parseInMain(argsType, args, Parsers.createFieldParserFactory());
    }

    public static <T> T parseInMain (Class<T> argsType, String[] args, FieldParserFactory parsers)
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
        return parse(argsType, args, Parsers.createFieldParserFactory());

    }

    public static <T> T parse (Class<T> argsType, String[] args, FieldParserFactory parsers)
        throws YarrgParseException
    {
        return new Command<T>(argsType, parsers).parse(args);
    }
}
