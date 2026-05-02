package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class WorkflowAccessGuard {

    private final boolean requireToken;
    private final String readToken;
    private final LocalizedMessages localizedMessages;

    public WorkflowAccessGuard(@Value("${career.workflow.access.require-token:false}") boolean requireToken,
                               @Value("${career.workflow.access.read-token:}") String readToken,
                               LocalizedMessages localizedMessages) {
        this.requireToken = requireToken;
        this.readToken = readToken == null ? "" : readToken.trim();
        this.localizedMessages = localizedMessages;

        if (this.requireToken && this.readToken.isBlank()) {
            throw new IllegalStateException("career.workflow.access.read-token must be configured when workflow access token protection is enabled.");
        }
    }

    public void verifyReadAccess(String providedToken) {
        if (!requireToken) {
            return;
        }

        String normalizedToken = providedToken == null ? "" : providedToken.trim();
        if (!constantTimeEquals(readToken, normalizedToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, localizedMessages.get("errors.request.forbidden"));
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }
}
