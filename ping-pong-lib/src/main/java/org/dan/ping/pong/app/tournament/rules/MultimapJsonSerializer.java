package org.dan.ping.pong.app.tournament.rules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Multimap;

import java.io.IOException;

public class MultimapJsonSerializer<K, V> extends JsonSerializer<Multimap<K, V>> {
    @Override
    public void serialize(Multimap<K, V> value,
            JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException {
        if (value.isEmpty()) {
            gen.writeNull();
            return;
        }
        gen.writeStartObject();
        for (K key : value.keySet()) {
            gen.writeObjectField(key.toString(), value.get(key));
        };
        gen.writeEndObject();
    }
}
