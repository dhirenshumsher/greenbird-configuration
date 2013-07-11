package com.greenbird.configuration;

import com.greenbird.GreenbirdException;

public class GreenbirdConfigurationException extends GreenbirdException {
    public GreenbirdConfigurationException() {
        super();
    }

    public GreenbirdConfigurationException(String message) {
        super(message);
    }

    public GreenbirdConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GreenbirdConfigurationException(Throwable cause) {
        super(cause);
    }
}
