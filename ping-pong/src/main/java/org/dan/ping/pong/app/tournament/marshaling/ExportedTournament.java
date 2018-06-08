package org.dan.ping.pong.app.tournament.marshaling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = ExportedTournamentJune_8th_2018.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExportedTournamentJune_8th_2018.class, name = "June8th2018"),
        })
public interface ExportedTournament {
}
