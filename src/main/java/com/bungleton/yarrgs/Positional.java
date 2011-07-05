//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks a field as a required argument with no flag. The value indicates where it falls
 * relative to other required required arguments. 0 is the first, 1 the second and so on. If the
 * value is negative, that indicates the argument comes after any arguments taken by an
 * {@link Unmatched} field; -1 is the last argument, -2 the second to last, and so on.</p>
 *
 * <p>If a field has both <code>@Positional</code> and <code>@Unmatched</code> that means it
 * requires at least one argument, and that it will consume any additional non-optional arguments
 * in the command. That allows for arguments like the sources field on the unix <code>cp</code>
 * command.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Positional {
    int value();
}
