package org.dan.ping.pong.sys.error;

import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.FORBIDDEN_403;
import static org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.UNAUTHORIZED_401;

import lombok.Getter;
import org.dan.ping.pong.app.tournament.BadStateError;
import org.dan.ping.pong.app.tournament.State;

import java.util.UUID;

@Getter
public class PiPoEx extends RuntimeException {
    private final int status;
    private final Object clientMessage;

    public PiPoEx(int status, Object clientMessage, Throwable cause) {
        super(clientMessage.toString(), cause);
        this.status = status;
        this.clientMessage = clientMessage;
    }

    public static PiPoEx notFound(String clientMessage) {
        return new PiPoEx(NOT_FOUND_404, clientMessage, null);
    }

    public static PiPoEx notAuthorized(String msg) {
        return new PiPoEx(UNAUTHORIZED_401, msg, null);
    }

    public static PiPoEx forbidden(String clientMessage) {
        return new PiPoEx(FORBIDDEN_403, clientMessage, null);
    }

    public static PiPoEx badRequest(String clientMessage) {
        return badRequest(
                new Error(UUID.randomUUID().toString(),
                clientMessage));
    }

    public static PiPoEx badRequest(Error error) {
        return badRequest(error, null);
    }

    public static PiPoEx badState(State state) {
        return new PiPoEx(BAD_REQUEST_400, BadStateError.of(state), null);
    }

    public static PiPoEx badRequest(Object clientMessage, Exception e) {
        return new PiPoEx(BAD_REQUEST_400, clientMessage, e);
    }

    public static PiPoEx internalError(String clientMessage) {
        return new PiPoEx(INTERNAL_SERVER_ERROR_500, clientMessage, null);
    }
}
