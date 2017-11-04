package org.dan.ping.pong.sys.db.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.bid.Uid;
import org.jooq.Converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MatchScoresConverter implements Converter<String, Map<Uid, List<Integer>>> {
    private static final ObjectMapper om = new ObjectMapper();
    private static final TypeReference<Map<Uid, List<Integer>>> type
            = new TypeReference<Map<Uid, List<Integer>>>() {};

    @Override
    @SneakyThrows
    public Map<Uid, List<Integer>> from(String s) {
        return om.readValue(s, type);
    }

    @Override
    @SneakyThrows
    public String to(Map<Uid, List<Integer>> scores) {
        return om.writeValueAsString(scores);
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<Map<Uid, List<Integer>>> toType() {
        return (Class<Map<Uid, List<Integer>>>) Collections.emptyMap().getClass();
    }
}
