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

    @Positional
    public String file;
}
