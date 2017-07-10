package org.dan.ping.pong.sys.sadmin;

import static java.security.MessageDigest.getInstance;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;

import javax.inject.Provider;

public class ShaProvider implements Provider<Sha> {
    @Override
    @Bean
    public Sha get() {
        return this::sha;
    }

    @SneakyThrows
    private String sha(String s) {
        return encodeHexString(getInstance("SHA-1").digest(s.getBytes()));
    }
}
