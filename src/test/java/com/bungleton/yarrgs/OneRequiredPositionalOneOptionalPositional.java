package com.bungleton.yarrgs;

import java.util.Date;

public class OneRequiredPositionalOneOptionalPositional
{
    @Positional
    public String injury;

    @Positional(position = 2, optional = true)
    public Date when = new Date();
}
