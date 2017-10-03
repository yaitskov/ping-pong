package org.dan.ping.pong.app.tournament.rules;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.util.collection.StreamsP.asStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Iterator;

@RequiredArgsConstructor
public class MultimapJsonDeserializer<V> extends JsonDeserializer<Multimap<String, V>> {
    private final Class<V> v;

    @Override
    public Multimap<String, V> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        final Multimap<String, V> result = HashMultimap.create();
        JsonNode node = p.getCodec().readTree(p);
        ;
        Iterator<String> fields = node.fieldNames();
        while (fields.hasNext()) {
            final String key = fields.next();
            result.putAll(key, asStream(node.get(key).iterator())
                    .map(n -> {
                        try {
                            return p.getCodec().treeToValue(n, v);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(toList()));
        }
        return result;
    }
}
