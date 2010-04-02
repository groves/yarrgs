package com.bungleton.yarrgs;

import org.junit.Test;

import static org.junit.Assert.fail;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertNotNull;

public class TestArgParser
{
    @Test
    public void parseNoArgs ()
        throws YarrgParseException
    {
        assertNotNull(Yarrgs.parse(EmptyArgs.class, new String[0]));
    }

    @Test
    public void parseOneFlag ()
        throws YarrgParseException
    {
        assertFalse(Yarrgs.parse(OneFlag.class, new String[0]).verbose);
        assertTrue(Yarrgs.parse(OneFlag.class, new String[] { "-v" }).verbose);
        assertTrue(Yarrgs.parse(OneFlag.class, new String[] { "--verbose" }).verbose);
    }

    @Test
    public void parseOneString ()
        throws YarrgParseException
    {
        assertNull(Yarrgs.parse(OneString.class, new String[0]).injury);
        assertEquals("hook", Yarrgs.parse(OneString.class, new String[] { "-i", "hook" }).injury);
        assertEquals("pegleg",
            Yarrgs.parse(OneString.class, new String[] { "--injury", "pegleg" }).injury);

    }

    @Test
    public void parsePositional ()
        throws YarrgParseException
    {
        assertEquals("hook", Yarrgs.parse(OnePositional.class, new String[] { "hook" }).injury);
    }

    @Test
    public void parseOptionalPositonal ()
        throws YarrgParseException
    {
        OneRequiredPositionalOneOptionalPositional result =
            Yarrgs.parse(OneRequiredPositionalOneOptionalPositional.class,
                new String[] { "eyepatch" });
        assertEquals(result.injury, "eyepatch");
        assertEquals(result.drink, "grog");
        result = Yarrgs.parse(OneRequiredPositionalOneOptionalPositional.class,
            new String[] { "pegleg", "rum" });
        assertEquals(result.injury, "pegleg");
        assertEquals(result.drink, "rum");
    }

    @Test
    public void collectUnparsed ()
        throws YarrgParseException
    {
        assertTrue(Yarrgs.parse(AllUnparsed.class, new String[0]).extras.isEmpty());
        AllUnparsed unparsed = Yarrgs.parse(AllUnparsed.class, new String[] {"shiver", "timbers"});
        assertEquals(2, unparsed.extras.size());
        assertEquals("shiver", unparsed.extras.get(0));
        assertEquals("timbers", unparsed.extras.get(1));
    }

    @Test
    public void printUsage ()
    {
        try {
            Yarrgs.parse(OneString.class, new String[] { "--help" });
            fail();
        } catch (YarrgParseException e) {
            assertEquals("Usage: OneString [-i INJURY]\n" +
                         "  -i ,--injury   Type of injury the pirate has\n", e.getMessage());
        }
    }
}
