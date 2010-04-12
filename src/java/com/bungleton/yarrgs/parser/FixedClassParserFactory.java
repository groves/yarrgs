package com.bungleton.yarrgs.parser;

public abstract class FixedClassParserFactory<T> extends SpecificClassParserFactory
{
    public FixedClassParserFactory (Class<T> klass)
    {
        super(klass);
    }

    @Override
    public Parser<T> createParser (Class<?> klass)
    {
        return new SingleArgParser<T>() {
            @Override public T parse (String arg) {
                return FixedClassParserFactory.this.parse(arg);
            }
        };
    }

    protected abstract T parse (String arg);
}
