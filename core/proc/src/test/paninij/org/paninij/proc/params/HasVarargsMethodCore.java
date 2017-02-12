package org.paninij.proc.codegen;

import org.paninij.lang.Capsule;

@Capsule
public class HasVarargsMethodCore
{
    public Object foo(Object o, Object i) {
        return new Object();
    }

    public Object primitiveArg(int i)
    {
        return new Object();
    }

    public Object arrayArg(Object[] arr) {
        return new Object();
    }

    public Object primitiveArrayArg(int[] arr) {
        return new Object();
    } 

    public Object variadicArg(int i, int... integers) {
        return new Object();
    } 
}
