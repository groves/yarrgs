package com.bungleton.yarrgs.argument;


public class HelpArgument extends OptionArgument
{
    public HelpArgument ()
    {
        super(null, "-h", "--help");
    }

    @Override
    public String getShortArgumentDescriptor ()
    {
        return "-h";
    }

    @Override
    public String getUsage ()
    {
        return "Print this help message and exit";
    }

    @Override
    public String getFullArgumentDescriptor ()
    {
        return "-h, --help";
    }
}
