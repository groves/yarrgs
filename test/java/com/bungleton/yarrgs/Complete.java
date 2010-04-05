package com.bungleton.yarrgs;

import java.util.Date;

public class Complete
{
    public boolean verbose;

    public boolean longFormat;

    public Date start, end;

    @Positional
    public String file;
}
