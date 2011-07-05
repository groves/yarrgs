//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs.parser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.HashMap;
import java.util.Map;

public class MapParserFactory
    implements FieldParserFactory
{
    public MapParserFactory (ClassParserFactory base)
    {
        _base = base;
    }

    @Override
    public boolean handles (Field f)
    {
        Type t = f.getGenericType();
        if (!(t instanceof ParameterizedType) || !
            Map.class.isAssignableFrom(f.getType())) {
            return false;
        }
        Type[] actualTypes = ((ParameterizedType)t).getActualTypeArguments();
        return _base.handles((Class<?>)actualTypes[0]) && _base.handles((Class<?>)actualTypes[1]);
    }

    @Override
    public Parser<?> createParser (Field f)
    {
        Type[] actualTypes = ((ParameterizedType)f.getGenericType()).getActualTypeArguments();
        final Class<?> keyType = (Class<?>)actualTypes[0];
        final Class<?> valType = (Class<?>)actualTypes[1];
        return new Parser<Map<?, ?>>() {
            protected final Map<Object, Object> vals = new HashMap<Object, Object>();
            @Override public void add (String arg) {
                int eqIdx = arg.indexOf('=');
                if (eqIdx == -1) {
                    throw new RuntimeException("Expected key and value separated by '=', not '" +
                        arg + "'");
                }
                Parser<?> keyParser = _base.createParser(keyType);
                keyParser.add(arg.substring(0, eqIdx));
                Parser<?> valParser = _base.createParser(valType);
                valParser.add(arg.substring(eqIdx + 1));
                vals.put(keyParser.getResult(), valParser.getResult());
            }

            @Override
            public Map<?, ?> getResult () {
                return vals;
            }};
    }

    protected final ClassParserFactory _base;
}
