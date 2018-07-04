package org.dan.ping.pong.sys.ctx;

import java.time.Duration;

public class DurationEditorSupport extends TypeSafePropertyEditor<Duration> {
    @Override
    protected Duration getValueFromString(String text) {
        return Duration.parse(text.toUpperCase());
    }
}
