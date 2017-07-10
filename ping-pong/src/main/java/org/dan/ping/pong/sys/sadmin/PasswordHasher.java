package org.dan.ping.pong.sys.sadmin;

import java.util.function.BiFunction;

import javax.inject.Inject;

public class PasswordHasher implements BiFunction<String, String, String> {
    @Inject
    private Sha sha;

    @Override
    public String apply(String password, String salt) {
        return sha.apply(password + salt);
    }
}
