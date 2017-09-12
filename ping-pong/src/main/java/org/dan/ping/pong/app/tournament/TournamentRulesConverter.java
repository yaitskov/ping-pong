package org.dan.ping.pong.app.tournament;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.jooq.Converter;

public class TournamentRulesConverter implements Converter<String, TournamentRules> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public TournamentRules from(String databaseObject) {
        return mapper.readValue(databaseObject, TournamentRules.class);
    }

    @Override
    @SneakyThrows
    public String to(TournamentRules userObject) {
        return mapper.writeValueAsString(userObject);
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<TournamentRules> toType() {
        return TournamentRules.class;
    }
}
