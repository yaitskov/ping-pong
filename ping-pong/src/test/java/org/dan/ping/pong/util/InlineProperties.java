
package org.dan.ping.pong.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ByteArrayResource;

public class InlineProperties extends PropertyPlaceholderConfigurer {
    public static ByteArrayResource asResource(ImmutableMap<String, ?> nameValue) {
        return new ByteArrayResource(toBytes(nameValue));
    }

    public static ByteArrayResource asResource(String key, Object value) {
        return new ByteArrayResource(toBytes(ImmutableMap.of(key, value)));
    }

    public static byte[] toBytes(ImmutableMap<String, ?> nameValue) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        for (Map.Entry<String, ?> entry : nameValue.entrySet()) {
            ps.append(entry.getKey()).append('=')
                    .append(entry.getValue().toString())
                    .append('\n');
        }
        ps.close();
        return out.toByteArray();
    }

    public static String unwrap(String propertyRef) {
        return propertyRef.substring(2, propertyRef.length() - 1);
    }

    public InlineProperties(ImmutableMap<String, ?> nameValue) {
        setLocations(asResource(nameValue));
    }

    public InlineProperties(String key, Object value) {
        setLocations(asResource(key, value));
    }
}
