package com.tradesphere.walletservice.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * Standardized RFC 9457 error response payload extending Spring's {@link ProblemDetail}.
 * Explicitly defines custom properties as class fields so OpenAPI/Swagger can introspect the exact schema.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse extends ProblemDetail {

    private final String errorCode;
    private final OffsetDateTime timestamp;
    private final Map<String, Object> metadata;
    private final List<ValidationError> errors;

    public ApiErrorResponse(HttpStatus status, String errorCode, String title, String detail,
                            String path, Map<String, Object> metadata, List<ValidationError> errors) {
        super(status.value());
        this.setTitle(title);
        this.setDetail(detail);
        this.setInstance(URI.create(path));
        this.errorCode = errorCode;
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        this.metadata = (metadata != null && !metadata.isEmpty()) ? metadata : null;
        this.errors = (errors != null && !errors.isEmpty()) ? errors : null;
    }
}
