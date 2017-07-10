package org.dan.ping.pong.util;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Reflector {
    @SuppressWarnings("unchecked")
    public static <T> Class<T> genericSuperClass(final Class klas, int index) {
        Class superKlas = klas;
        while (true) {
            if (superKlas == Object.class) {
                throw new IllegalArgumentException("Class " + klas
                        + " is not an ancestor of a generic one");
            }
            Type type = superKlas.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Object result = ((ParameterizedType) type).getActualTypeArguments()[index];
                if (result instanceof Class) {
                    return (Class<T>) result;
                } else {
                    throw new IllegalArgumentException("Class " + klas
                            + " is not an ancestor of a generic one");
                }
            } else {
                superKlas = superKlas.getSuperclass();
            }
        }
    }

    public static <T> Class<T> genericSuperClass(final Class klas) {
        return genericSuperClass(klas, 0);
    }
}
