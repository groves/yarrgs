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
