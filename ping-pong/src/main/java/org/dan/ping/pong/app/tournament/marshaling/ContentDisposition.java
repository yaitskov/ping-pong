package org.dan.ping.pong.app.tournament.marshaling;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encode;

import lombok.SneakyThrows;

import java.util.regex.Pattern;

public class ContentDisposition {
    private static Pattern ASCII_NAME_PATTERN = Pattern.compile(
            "[^a-zA-Z0-9 .,:+%@$!;_-]");

    @SneakyThrows
    public static String headerBody(String fileName) {
        return "attachment; filename=\""
                + ASCII_NAME_PATTERN.matcher(fileName).replaceAll("_")
                + "\"; filename*=utf-8''"
                + encode(fileName, UTF_8.name());
    }
}
