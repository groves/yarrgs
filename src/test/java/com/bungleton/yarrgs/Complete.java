//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Complete
{
    public boolean verbose;

    public boolean longFormat;

    public Date start, end;

    public List<Injury> injuries;

    public Map<String, Integer> divisions;

    @Positional(0)
    public String file;
}
