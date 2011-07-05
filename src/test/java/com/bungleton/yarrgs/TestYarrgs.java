//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the top level interface through {@link Yarrgs}
 */
public class TestYarrgs
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
    public void collectUnparsed ()
        throws YarrgParseException
    {
        assertNull(Yarrgs.parse(AllUnmatched.class, new String[0]).extras);
        AllUnmatched unparsed = Yarrgs.parse(AllUnmatched.class, new String[] {"shiver", "timbers"});
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
            assertEquals("Usage: OneString [-i INJURY,-h] ", e.getUsage());
            assertEquals("Options:\n" +
                "  -i INJURY, --injury INJURY\n    Type of injury the pirate has\n" +
                "  -h, --help         Print this help message and exit\n\n",
                e.getMessage());
        }
    }

    @Test
    public void parseDate ()
        throws YarrgParseException
    {
        Date parsed = Yarrgs.parse(OneDate.class, new String[] { "--start", "2010-04-01" }).start;
        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        assertEquals(2010, cal.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void parseMalformedDate ()
        throws YarrgParseException
    {
        try {
            Yarrgs.parse(OneDate.class, new String[] { "--start", "20100401" });
            fail();
        } catch (YarrgParseException ex) {
            assertEquals("Usage: OneDate [-s START,-h] \n'20100401' doesn't match yyyy-MM-dd",
                ex.getExitMessage());
        }
    }

    @Test
    public void parseEnum ()
        throws YarrgParseException
    {
        assertEquals(Injury.eyepatch,
            Yarrgs.parse(OneEnum.class, new String[] { "-i", "eyepatch" }).injury);
        try {
            Yarrgs.parse(OneEnum.class, new String[] { "-ihoooook" });
            fail();
        } catch (YarrgParseException ex) {
            assertEquals("Expecting one of pegleg|hook|eyepatch, not 'hoooook'", ex.getMessage());
        }
    }

    @Test
    public void parseEnumUnmatched ()
        throws YarrgParseException
    {
        List<Injury> injuries =
            Yarrgs.parse(EnumUnmatched.class, new String[] { "eyepatch", "hook" }).injuries;
        assertEquals(2, injuries.size());
        assertEquals(Injury.eyepatch, injuries.get(0));
        assertEquals(Injury.hook, injuries.get(1));
    }

    @Test
    public void parseLastPositional ()
        throws YarrgParseException
    {
        LastPositional lp = Yarrgs.parse(LastPositional.class, new String[] {"2010-04-01", "pegleg"});
        Calendar cal = Calendar.getInstance();
        cal.setTime(lp.time);
        assertEquals(2010, cal.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Injury.pegleg, lp.injury);
    }

    @Test
    public void parseLastPositionalWithUnmatched ()
        throws YarrgParseException
    {
        LastPositional lp = Yarrgs.parse(LastPositional.class, new String[] {"2010-04-01", "Charlie", "Chelsea", "pegleg"});
        Calendar cal = Calendar.getInstance();
        cal.setTime(lp.time);
        assertEquals(2010, cal.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Injury.pegleg, lp.injury);
        assertEquals(2, lp.names.size());
    }

    @Test
    public void parseMissingLastPositional ()
        throws YarrgParseException
    {
        try {
            Yarrgs.parse(LastPositional.class, new String[] {"hook"});
            fail();
        } catch (YarrgParseException ex) {
            assertEquals("'hook' doesn't match yyyy-MM-dd", ex.getMessage());
        }
    }

    @Test(expected = YarrgParseException.class)
    public void parseBadLastPositional ()
        throws YarrgParseException
    {
        Yarrgs.parse(LastPositional.class, new String[] { "2010-04-01", "Charlie" });
    }

    @Test(expected = YarrgParseException.class)
    public void parseMissingSource ()
        throws YarrgParseException
    {
        Yarrgs.parse(cp.class, new String[] {"dest"});
    }

    @Test
    public void parseMultipleSourceCp ()
        throws YarrgParseException
    {
        cp copy = Yarrgs.parse(cp.class, new String[] {"source1", "source2", "dest"});
        assertEquals(2, copy.sourceFiles.size());
        assertEquals(new File("source1"), copy.sourceFiles.get(0));
        assertEquals(new File("source2"), copy.sourceFiles.get(1));
        assertEquals(new File("dest"), copy.destination);
        assertEquals(false, copy.recursive);
    }
}
