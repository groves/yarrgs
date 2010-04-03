package com.bungleton.yarrgs;

import com.bungleton.yarrgs.parser.CLIParser;

public class Yarrgs
{
    public static <T> T parseInMain (Class<T> argsType, String[] args)
    {
        try {
            return parse(argsType, args);
        } catch (YarrgParseException e) {
            System.err.println(e.getUsage() + "\n" + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    public static <T> T parse (Class<T> argsType, String[] args)
        throws YarrgParseException
    {
        T t;
        try {
            t = argsType.newInstance();
        } catch (Exception e) {
            throw new YarrgConfigurationException("'" + argsType
                + "' must have a public no-arg constructor", e);
        }
        new CLIParser(t).parse(args);
        return t;
    }
}
