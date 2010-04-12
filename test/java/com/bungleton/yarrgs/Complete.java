package com.bungleton.yarrgs;

import java.util.Date;
import java.util.List;

public class Complete
{
    public boolean verbose;

    public boolean longFormat;

    public Date start, end;

    public List<Injury> injuries;

    @Positional
    public String file;
}
