/**
 * Copyright (c) 2016 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.util;

/**
 * @author semancik
 *
 */
@FunctionalInterface
public interface Foreachable<T> {

    /**
     * Will call processor for every element in the instance.
     * This is NOT recursive. E.g. in case of collection of collections
     * the processor will NOT be called for elements of the inner collections.
     * If you need recursion please have a look at Visitor.
     */
    void foreach(Processor<T> processor);

}
