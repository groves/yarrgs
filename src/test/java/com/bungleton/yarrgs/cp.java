package com.bungleton.yarrgs;

import java.io.File;
import java.util.List;

public class cp
{
    @Usage("Should subdirectories of directores in sourceFiles be copied")
    public boolean recursive;

    @Usage("Files and directories to be copied") @Positional(0) @Unmatched
    public List<File> sourceFiles;

    @Usage("Directory to which sourceFiles should be copied") @Positional(-1)
    public File destination;

    public static void main (String[] args)
    {
        cp cp = Yarrgs.parseInMain(cp.class, args);
        System.out.println("Recursive: " + cp.recursive);
        System.out.println("Source files: " + cp.sourceFiles);
        System.out.println("Dest: " + cp.destination);
    }
}
