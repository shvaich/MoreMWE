package me.shvaich.moremwe.utils;

import me.shvaich.moremwe.MoreMWE;

public final class MoreMWEException extends RuntimeException {
    private static final String MESSAGE_HEAD = MoreMWE.NAME + " has encountered an unexpected error.";

    public MoreMWEException() {
        super(MESSAGE_HEAD);
    }

    public MoreMWEException(Throwable cause) {
        super(MESSAGE_HEAD, cause);
    }

    public MoreMWEException sendDebug(String message, Object... params) {
        MoreMWE.LOGGER.debug(message, params);
        return this;
    }
}
