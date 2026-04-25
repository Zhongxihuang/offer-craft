package com.workspace.codeforgeai.common.api;

import java.util.List;

public class ApiValidationException extends RuntimeException {

    private final List<ApiErrorDetail> details;

    public ApiValidationException(String message, List<ApiErrorDetail> details) {
        super(message);
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public List<ApiErrorDetail> details() {
        return details;
    }
}
