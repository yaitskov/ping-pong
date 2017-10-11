package org.dan.ping.pong.app.tournament;

import static java.lang.Integer.compare;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.IOException;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Uid implements Comparable<Uid> {
    private final int id;

    // jax-rsp
    public static Uid valueOf(String s) {
        return new Uid(Integer.valueOf(s));
    }

    public static Module jacksonMarshal() {
        return new SimpleModule("uid")
                .addKeyDeserializer(Uid.class, new UidKeyDeserializer())
                .addKeySerializer(Uid.class, new UidKeySerializer())
                .addSerializer(new Serializer())
                .addDeserializer(Uid.class, new Deserializer());
    }

    @Override
    public int compareTo(Uid o) {
        return compare(id, o.id);
    }

    private static class Serializer extends StdSerializer<Uid> {
        protected Serializer() {
            super(Uid.class);
        }

        public void serialize(Uid value, JsonGenerator gen,
                SerializerProvider serializers)
                throws IOException {
            gen.writeNumber(value.getId());
        }
    }

    private static class Deserializer extends JsonDeserializer<Uid> {
        public Uid deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new Uid(p.readValueAs(Integer.class));
        }
    }

    private static class UidKeyDeserializer extends KeyDeserializer {
        @Override
        public Uid deserializeKey(String key, DeserializationContext ctxt) {
            return new Uid(Integer.valueOf(key));
        }
    }

    private static class UidKeySerializer extends StdSerializer<Uid> {
        public UidKeySerializer() {
            super(Uid.class);
        }

        @Override
        @SneakyThrows
        public void serialize(Uid value, JsonGenerator gen, SerializerProvider provider) {
            gen.writeFieldId(value.getId());
        }
    }
}
