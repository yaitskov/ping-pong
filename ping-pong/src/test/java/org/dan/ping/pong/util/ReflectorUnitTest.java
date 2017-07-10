package org.dan.ping.pong.util;

import static org.dan.ping.pong.util.Reflector.genericSuperClass;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReflectorUnitTest {
    static abstract class Base<X> {
    }

    static class DirectString extends Base<String> {
    }

    @Test
    public void extractTypeParameterFromDirect() {
        assertEquals(String.class, genericSuperClass(DirectString.class));
    }

    static class FromNonGeneric extends DirectString {
    }

    @Test
    public void extractTypeParameterFromInDirect() {
        assertEquals(String.class, genericSuperClass(FromNonGeneric.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractTypeParameterFromString() {
        genericSuperClass(String.class);
    }
}
