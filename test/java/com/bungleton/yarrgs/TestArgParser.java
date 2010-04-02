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
        assertNotNull(Yarrgs.parse(EmptyArgs.class, new String[0]));
    }

    @Test
    public void parseOneFlag ()
    {
        assertFalse(Yarrgs.parse(OneFlag.class, new String[0]).verbose);
        assertTrue(Yarrgs.parse(OneFlag.class, new String[] { "-v" }).verbose);
        assertTrue(Yarrgs.parse(OneFlag.class, new String[] { "--verbose" }).verbose);
    }

    @Test
    public void parseOneString ()
    {
        assertNull(Yarrgs.parse(OneString.class, new String[0]).injury);
        assertEquals("hook", Yarrgs.parse(OneString.class, new String[] { "-i", "hook" }).injury);
        assertEquals("pegleg",
            Yarrgs.parse(OneString.class, new String[] { "--injury", "pegleg" }).injury);

    }

    @Test
    public void parsePositional ()
    {
        assertEquals("hook", Yarrgs.parse(OnePositional.class, new String[] { "hook" }).injury);
    }
}
