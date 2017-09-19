package org.dan.ping.pong.sys.error;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TemplateError extends Error {
    private Map<String, Object> params;

    public TemplateError() {}

    public TemplateError(String message, Map<String, Object> params) {
        super(message);
        this.params = params;
    }
}
