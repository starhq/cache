package com.star.cache.exception;

/**
 * 缓存异常
 * <p>
 * Created by starhq on 2016/11/24.
 */
public class CacheException extends RuntimeException {

    private static final long serialVersionUID = 5514634434623320575L;

    public CacheException() {
        super();
    }

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheException(Throwable cause) {
        super(cause);
    }

    protected CacheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
