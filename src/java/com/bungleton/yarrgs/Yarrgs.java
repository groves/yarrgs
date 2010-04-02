package com.bungleton.yarrgs;

public class Yarrgs
{
    public static <T> T parse (Class<T> argsType, String[] args)
    {
        T t;
        try {
            t = argsType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        new ArgParser(t).parse(args);
        return t;
    }
}
