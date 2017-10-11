package org.dan.ping.pong.app.place;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Pid {
    private final int pid;

    public String toString() {
        return "pid(" + pid + ")";
    }

    public static Module jacksonMarshal() {
        return new SimpleModule("pid")
                .addSerializer(new Serializer())
                .addDeserializer(Pid.class, new Deserializer());
    }

    private static class Serializer extends StdSerializer<Pid> {
        protected Serializer() {
            super(Pid.class);
        }

        public void serialize(Pid value, JsonGenerator gen,
                SerializerProvider serializers)
                throws IOException {
            gen.writeNumber(value.getPid());
        }
    }

    private static class Deserializer extends JsonDeserializer<Pid> {
        public Pid deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return new Pid(p.readValueAs(Integer.class));
        }
    }
}
