package com.bungleton.yarrgs;

import java.io.File;
import java.util.List;

public class cp
{
    public boolean recursive;

    @Positional(0) @Unmatched
    public List<File> sourceFiles;

    @Positional(-1)
    public File destination;
}
