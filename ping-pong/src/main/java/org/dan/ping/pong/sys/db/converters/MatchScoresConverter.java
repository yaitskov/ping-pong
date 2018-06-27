package org.dan.ping.pong.sys.db.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.bid.Bid;
import org.jooq.Converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MatchScoresConverter implements Converter<String, Map<Bid, List<Integer>>> {
    private static final ObjectMapper om = new ObjectMapper();
    private static final TypeReference<Map<Bid, List<Integer>>> type
            = new TypeReference<Map<Bid, List<Integer>>>() {};

    @Override
    @SneakyThrows
    public Map<Bid, List<Integer>> from(String s) {
        return om.readValue(s, type);
    }

    @Override
    @SneakyThrows
    public String to(Map<Bid, List<Integer>> scores) {
        return om.writeValueAsString(scores);
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<Map<Bid, List<Integer>>> toType() {
        return (Class<Map<Bid, List<Integer>>>) Collections.emptyMap().getClass();
    }
}
