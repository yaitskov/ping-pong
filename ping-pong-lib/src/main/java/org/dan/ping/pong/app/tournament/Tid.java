package org.dan.ping.pong.app.tournament;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.place.Pid;

import java.io.IOException;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Tid {
    private final int tid;

    // jax-rsp
    public static Tid valueOf(String s) {
        return new Tid(Integer.valueOf(s));
    }

    public String toString() {
        return String.valueOf(tid);
    }

    public static Module jacksonMarshal() {
        return new SimpleModule("tid")
                .addSerializer(new Serializer())
                .addDeserializer(Tid.class, new Deserializer());
    }

    private static class Serializer extends StdSerializer<Tid> {
        protected Serializer() {
            super(Tid.class);
        }

        public void serialize(Tid value, JsonGenerator gen,
                SerializerProvider serializers)
                throws IOException {
            gen.writeNumber(value.getTid());
        }
    }

    private static class Deserializer extends JsonDeserializer<Tid> {
        public Tid deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new Tid(p.readValueAs(Integer.class));
        }
    }
}
