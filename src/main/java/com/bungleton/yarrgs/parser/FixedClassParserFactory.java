package com.bungleton.yarrgs.parser;

public abstract class FixedClassParserFactory extends SpecificClassParserFactory
{
    public FixedClassParserFactory (Class<?>...klasses)
    {
        super(klasses);
    }

    @Override
    public Parser<?> createParser (Class<?> klass)
    {
        return new SingleArgParser<Object>() {
            @Override public Object parse (String arg) {
                return FixedClassParserFactory.this.parse(arg);
            }
        };
    }

    protected abstract Object parse (String arg);
}
