package com.bungleton.yarrgs;

public class OneRequiredPositionalOneOptionalPositional
{
    @Positional
    public String injury;

    @Positional(position = 2, optional = true)
    public String drink = "grog";
}
