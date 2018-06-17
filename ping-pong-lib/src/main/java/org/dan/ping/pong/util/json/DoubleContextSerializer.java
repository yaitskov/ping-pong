package org.dan.ping.pong.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor
@AllArgsConstructor
public class DoubleContextSerializer
        extends JsonSerializer<Double>
        implements ContextualSerializer {
    private int precision = 0;

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (precision == 0) {
            gen.writeNumber(value.doubleValue());
        } else {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(precision, RoundingMode.HALF_UP);
            gen.writeNumber(bd.doubleValue());
        }

    }
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        final Precision precision = property.getAnnotation(Precision.class);
        if (precision != null) {
            return new DoubleContextSerializer (precision.value());
        }
        return this;
    }

}
