package com.bungleton.yarrgs;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import com.bungleton.yarrgs.parser.Parser;
import com.bungleton.yarrgs.parser.Parsers;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;

public class TestCommand
{
    @Test(expected = YarrgConfigurationException.class)
    public void parseMalformedUnmatched ()
    {
        new Command<NonparameterizedUnmatched>(NonparameterizedUnmatched.class, Parsers.DEFAULT);
    }

    @Test(expected = YarrgConfigurationException.class)
    public void parseMissingUnmatchedParser ()
    {
        List<Parser<?>> parsers = new ArrayList<Parser<?>>();
        parsers.add(Parsers.INT);
        new Command<EnumUnmatched>(EnumUnmatched.class, parsers);
    }

    @Test
    public void parseMashedShortargs ()
        throws YarrgParseException
    {
        Complete complete = parseWithComplete("-lv", "filename");
        assertTrue(complete.verbose);
        assertTrue(complete.longFormat);
    }

    @Test(expected = YarrgHelpException.class)
    public void parseHelpFromMashedShortargs ()
        throws YarrgParseException
    {
        parseWithComplete("-lhv", "file");
    }

    @Test(expected = YarrgParseException.class)
    public void parseValueAmidstMashedShortargs ()
        throws YarrgParseException
    {
        parseWithComplete("-lsv", "2009-10-03", "file");
    }

    @Test
    public void parseValueAtTheEndOfMashedShortargs ()
        throws YarrgParseException
    {
        Complete complete = parseWithComplete("-lvs", "2009-10-03", "filename");
        assertTrue(complete.verbose);
        assertTrue(complete.longFormat);
    }

    @Test
    public void parseStdinFilename ()
        throws YarrgParseException
    {
        assertEquals("-", parseWithComplete("-").file);
    }

    @Test(expected = YarrgParseException.class)
    public void parseMissingPositional ()
        throws YarrgParseException
    {
        parseWithComplete("-lv");
    }

    @Test(expected = YarrgParseException.class)
    public void parseDoubleFlag ()
        throws YarrgParseException
    {
        parseWithComplete("-ll", "filename");
    }

    protected static Complete parseWithComplete (String... args)
        throws YarrgParseException
    {
        return new Command<Complete>(Complete.class, Parsers.DEFAULT).parse(args);
    }
}
