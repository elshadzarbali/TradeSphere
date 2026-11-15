package com.tradesphere.walletservice.exception;

import com.tradesphere.walletservice.exception.model.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * Abstract base class for all domain and business exceptions within the Wallet microservice.
 * Carries machine-readable error codes, HTTP status mappings, and dynamic context properties.
 */
@Getter
public abstract class BaseDomainException extends RuntimeException {

    private final String errorCode;
    private final String title;
    private final HttpStatus httpStatus;
    private final transient Map<String, Object> properties;

    protected BaseDomainException(ErrorCode errorCode, String message) {
        this(errorCode, message, Collections.emptyMap(), null);
    }

    protected BaseDomainException(ErrorCode errorCode, String message, Map<String, Object> properties) {
        this(errorCode, message, properties, null);
    }

    protected BaseDomainException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, Collections.emptyMap(), cause);
    }

    protected BaseDomainException(ErrorCode errorCode, String message, Map<String, Object> properties, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.title = errorCode.getTitle();
        this.httpStatus = errorCode.getHttpStatus();
        this.properties = (properties != null && !properties.isEmpty()) ? Map.copyOf(properties) : Collections.emptyMap();
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }
}
