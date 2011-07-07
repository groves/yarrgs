//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

import com.bungleton.yarrgs.parser.Command;
import com.bungleton.yarrgs.parser.FieldParserFactory;
import com.bungleton.yarrgs.parser.Parsers;

/**
 * Main entry point for parsing args. Call {@link #parseInMain} to parse arguments in your main
 * method.
 */
public class Yarrgs
{
    private Yarrgs() {}// not instantiable

    /**
     * Parses <code>args</code> into an instance of <code>argsType</code> using
     * {@link Parsers#createFieldParserFactory()}. Calls <code>System.exit(1)</code> if the user
     * supplied bad arguments after printing a reason to <code>System.err</code>. Thus, this is
     * suitable to be called from a <code>main</code> method that's parsing arguments.
     */
    public static <T> T parseInMain (Class<T> argsType, String[] args)
    {
        return parseInMain(argsType, args, Parsers.createFieldParserFactory());
    }

    /**
     * Parses <code>args</code> into an instance of <code>argsType</code> using
     * <code>parsers</code>. Calls <code>System.exit(1)</code> if the user supplied bad arguments
     * after printing a reason to <code>System.err</code>. Thus, this is suitable to be called
     * from a <code>main</code> method that's parsing arguments.
     */
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

    /**
     * Parses <code>args</code> into an instance of <code>argsType</code> using
     * {@link Parsers#createFieldParserFactory()}. If the user supplied bad arguments,
     * <code>YarrgParseException</code> is thrown. If they asked for help,
     * <code>YarrgHelpException</code> is thrown. It's up to the caller to present the parse
     * failure to the user.
     */
    public static <T> T parse (Class<T> argsType, String[] args)
        throws YarrgParseException
    {
        return parse(argsType, args, Parsers.createFieldParserFactory());

    }

    /**
     * Parses <code>args</code> into an instance of <code>argsType</code> using
     * {@link Parsers#createFieldParserFactory()}. If the user supplied bad arguments,
     * <code>YarrgParseException</code> is thrown. If they asked for help,
     * <code>YarrgHelpException</code> is thrown. It's up to the caller to present the parse
     * failure to the user.
     */
    public static <T> T parse (Class<T> argsType, String[] args, FieldParserFactory parsers)
        throws YarrgParseException
    {
        return new Command<T>(argsType, parsers).parse(args);
    }
}
