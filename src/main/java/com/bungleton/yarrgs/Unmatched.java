package com.bungleton.yarrgs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Collects any non-flagged arguments not taken by a {@code Positional} field. This must be added
 * to a {@code List} field to allow it to collect multiple items. There can be only one
 * <code>@Unmatched</code> in a class.
 *
 * @see {@link Positional} - for how combining these annotations work.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Unmatched {
}
