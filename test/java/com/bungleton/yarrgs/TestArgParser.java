package com.bungleton.yarrgs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertNotNull;

public class TestArgParser
{
    @Test
    public void parseNoArgs ()
    {
        assertNotNull(ArgParser.parse(EmptyArgs.class, new String[0]));
    }

    @Test
    public void parseOneFlag ()
    {
        assertFalse(ArgParser.parse(OneFlag.class, new String[0]).verbose);
        assertTrue(ArgParser.parse(OneFlag.class, new String[] { "-v" }).verbose);
        assertTrue(ArgParser.parse(OneFlag.class, new String[] { "--verbose" }).verbose);
    }

    @Test
    public void parseOneString ()
    {
        assertNull(ArgParser.parse(OneString.class, new String[0]).injury);
        assertEquals("hook",
            ArgParser.parse(OneString.class, new String[] { "-i", "hook" }).injury);
        assertEquals("pegleg",
            ArgParser.parse(OneString.class, new String[] { "--injury", "pegleg" }).injury);

    }
}
