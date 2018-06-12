package org.dan.ping.pong.util;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import java.util.function.Supplier;

public class Integers {
    public static boolean odd(int n) {
        return (n & 1) == 1;
    }

    public static int parseInteger(String s, Supplier<String> errorMsgF) {
        try {
            return parseInt(s);
        } catch (NumberFormatException e) {
            throw badRequest(errorMsgF.get(), e);
        }
    }

    public static long parseLongInt(String s, Supplier<String> errorMsgF) {
        try {
            return parseLong(s);
        } catch (NumberFormatException e) {
            throw badRequest(errorMsgF.get(), e);
        }
    }
}
