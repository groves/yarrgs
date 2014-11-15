Yarrgs parses command-line arguments into a Java class that describes the expected arguments with
its fields and annotations.

Features
===========

  * Converts and validates int, boolean, Enum, String, Date, and File arguments. It also converts
    Lists and Maps of any of the validated fields.
  * Allows arguments to be required in a fixed position.
  * Collects unmatched arguments into a List of a validated type.
  * Needs minimal configuration and setup.
  * Generates and displays usage and help messages from the argument class automatically.

Usage
=====

Yarrgs is so simple a pirate could use it. First, write a class describing the arguments:

    public class cp {
        @Usage("Should subdirectories of directores in sourceFiles be copied")
        public boolean recursive;

        @Usage("Files and directories to be copied") @Positional(0) @Unmatched
        public List<File> sourceFiles;

        @Usage("Directory to which sourceFiles should be copied") @Positional(-1)
        public File destination;
    }

This class emulates a basic version of the Unix cp command. Every public field on it is assumed to
be an argument. Fields without `@Positional` and `@Unmatched` are optional. They're specified with
a flag eg `-r` or `--recursive` for the `recursive` field above. Flags are always the first letter
of the field for the short option and the full field for the long option.

If a field has a `@Positional` annotation, that means it's a required argument in a particular
position on the command line. `@Positional(0)`, like on the `sourceFiles` field, means the first
non-optional field is used for it. `@Positional(1)` would come after that field.

If a field has an `@Unmatched` annotation, it collects any non-optional non-positonal arguments on
the command line ie the extra stuff. Since there can be any number of extra arguments, a field with
`@Unmatched` must be a list. When `@Unmatched` is combined with `@Positional` on a field that means
there must be at least one argument for that field to satisfy the positional, and that any
additional arguments will be filled into it, like a regular unmatched. We're using that on
`sourceFiles` in the example to require at least one source file but allow many, just like the cp
command.

If a `@Positional` has a negative value that means the argument comes after all the unmatched arguments. For our example above, that makes `destination` the last argument after all the source files. `@Positional(-1)` is the last argument, `@Positional(-2)` the second to last, and so on.

With that explanation, we're ready to do the parsing. To do so, call `Yarrgs.parseInMain` from
a class with a main running the command:

    public static void main (String[] args) {
        cp copyArgs = Yarrgs.parseInMain(cp.class, args);
        System.out.println("Recursive: " + copyArgs.recursive);
        System.out.println("Source files: " + copyArgs.sourceFiles);
        System.out.println("Dest: " + copyArgs.destination);
    }

The above main just prints out what's parsed by the class:

    prompt$ cp README.md build.xml src
    Recursive: false
    Source files: [README.md, build.xml]
    Dest: src

Since `-r` wasn't specified, `recursive` is left at its default value, `false`. This is how default
values can be given for options. If nothing is specified for an option, Yarrgs leaves that field
alone.

If the correct arguments aren't specified to `parseInMain`, a help message is printed and
`System.exit(1)` is called:

    prompt$ cp README.md
    Usage: cp [-r,-h] sourceFiles [sourceFiles...] destination
    Required argument 'sourceFiles' missing

All commands also automatically get a help option specified with `-h` or `--help`:

    prompt$ cp --help
    Usage: cp [-r,-h] sourceFiles [sourceFiles...] destination
    Options:
      -r, --recursive    Should subdirectories of directores in sourceFiles be copied
      -h, --help         Print this help message and exit

    Positionals:
      sourceFiles        Files and directories to be copied
      destination        Directory to which sourceFiles should be copied

    Unmatched:
      [sourceFiles...]   Files and directories to be copied
