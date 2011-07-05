//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

import java.util.Date;
import java.util.List;

public class LastPositional
{
    @Positional(-1)
    public Injury injury;

    @Unmatched
    public List<String> names;

    @Positional(0)
    public Date time;
}
