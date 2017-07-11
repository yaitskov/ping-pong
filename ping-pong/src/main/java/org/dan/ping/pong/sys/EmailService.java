package org.dan.ping.pong.sys;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class EmailService {
    public void send(String email, String templateName, Map<String, String> params) {
        log.info("Send {} email to {} with {}", templateName, email, params);
    }
}
