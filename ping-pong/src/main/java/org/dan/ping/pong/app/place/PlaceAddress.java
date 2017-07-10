package org.dan.ping.pong.app.place;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceAddress {
    private Optional<Gps> gps;
    private String city;
    private String address;
    private Optional<String> email;
    private Optional<String> phone;

    public static class PlaceAddressBuilder {
        private Optional<Gps> gps = Optional.empty();
        private Optional<String> email = Optional.empty();
        private Optional<String> phone = Optional.empty();
    }
}
