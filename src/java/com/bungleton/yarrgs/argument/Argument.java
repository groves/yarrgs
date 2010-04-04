package com.bungleton.yarrgs.argument;

import java.lang.reflect.Field;

import com.bungleton.yarrgs.Usage;

public abstract class Argument
{
    public final Field field;

    public Argument (Field field)
    {
        this.field = field;
    }

    public String getUsage ()
    {
        Usage u = field.getAnnotation(Usage.class);
        return u == null ? "" : u.value();
    }

    public static StringBuilder wrap (StringBuilder builder, String str, int indentSize)
    {
        int wrapLength = LINE_LENGTH - indentSize;
        StringBuilder inBuild = new StringBuilder();
        while(inBuild.length() < indentSize) {
            inBuild.append(' ');
        }
        String indent = inBuild.toString();
        int position = 0;
        while ((str.length() - position) > wrapLength) {
            int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + position);
            builder.append(indent);
            if (spaceToWrapAt != -1) {
                builder.append(str.substring(position, spaceToWrapAt));
                position = spaceToWrapAt + 1;
            } else {
                builder.append(str.substring(position, wrapLength + position));
                position = wrapLength + position + 1;
            }
            builder.append('\n');
        }

        // Whatever is left in line is short enough to just pass through
        return builder.append(indent).append(str.substring(position));
    }

    public abstract String getShortArgumentDescriptor ();

    public String getFullArgumentDescriptor()
    {
        return getShortArgumentDescriptor();
    }

    public String getDetail ()
    {
        StringBuilder builder = new StringBuilder("  ").append(getFullArgumentDescriptor());
        String usage = getUsage();
        if (builder.length() <= 20 && builder.length() + usage.length() + 1 < LINE_LENGTH) {
            while(builder.length() < 20) {
                builder.append(' ');
            }
            builder.append(' ').append(usage).toString();
        } else {
            wrap(builder.append('\n'), usage, 4);
        }
        return builder.toString();
    }

    protected static final int LINE_LENGTH = 80;
}
