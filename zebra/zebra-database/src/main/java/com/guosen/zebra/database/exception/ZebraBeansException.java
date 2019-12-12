package com.guosen.zebra.database.exception;

import org.springframework.beans.BeansException;

/**
 * Zebra Bean异常类
 */
public class ZebraBeansException extends BeansException {

    public ZebraBeansException(String msg) {
        super(msg);
    }

    public ZebraBeansException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
