package org.dan.ping.pong.sys.error;

import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.FORBIDDEN_403;
import static org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.UNAUTHORIZED_401;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PiPoEx extends RuntimeException {
    private final int status;
    private final Error clientMessage;

    public PiPoEx(int status, Error clientMessage, Throwable cause) {
        super(clientMessage.toString(), cause);
        this.status = status;
        this.clientMessage = clientMessage;
    }

    public static PiPoEx notFound(String clientMessage) {
        return new PiPoEx(NOT_FOUND_404, new Error(clientMessage), null);
    }

    public static PiPoEx notAuthorized(String msg) {
        return new PiPoEx(UNAUTHORIZED_401, new Error(msg), null);
    }

    public static PiPoEx forbidden(String clientMessage) {
        return new PiPoEx(FORBIDDEN_403, new Error(clientMessage), null);
    }

    public static PiPoEx badRequest(String clientMessage) {
        return badRequest(
                new Error(UUID.randomUUID().toString(),
                clientMessage));
    }

    public static PiPoEx badRequest(String clientMessage, Exception e) {
        return badRequest(
                new Error(UUID.randomUUID().toString(),
                        clientMessage), e);
    }

    public static PiPoEx badRequest(Error error) {
        return badRequest(error, null);
    }

    public static PiPoEx badRequest(Error clientMessage, Exception e) {
        return new PiPoEx(BAD_REQUEST_400, clientMessage, e);
    }

    public static PiPoEx internalError(String clientMessage) {
        return new PiPoEx(INTERNAL_SERVER_ERROR_500, new Error(clientMessage), null);
    }
}
