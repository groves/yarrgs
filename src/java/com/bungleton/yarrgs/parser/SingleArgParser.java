package com.bungleton.yarrgs.parser;

public abstract class SingleArgParser<T>
    implements Parser<T>
{
    public abstract T parse(String arg);

    @Override
    public void add (String arg)
    {
        if (_result == null) {
            _result = parse(arg);
        } else {
            throw new RuntimeException("Expected only once");
        }
    }

    @Override
    public T getResult ()
    {
        return _result;
    }

    protected T _result;
}
