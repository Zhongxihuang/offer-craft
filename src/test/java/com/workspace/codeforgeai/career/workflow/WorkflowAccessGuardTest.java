package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowAccessGuardTest {

    @Test
    void constructorRejectsMissingTokenWhenProtectionIsEnabled() {
        assertThrows(
                IllegalStateException.class,
                () -> new WorkflowAccessGuard(true, " ", localizedMessages())
        );
    }

    @Test
    void verifyReadAccessAllowsRequestsWhenProtectionIsDisabled() {
        WorkflowAccessGuard guard = new WorkflowAccessGuard(false, "", localizedMessages());

        assertDoesNotThrow(() -> guard.verifyReadAccess(null));
    }

    @Test
    void verifyReadAccessAllowsMatchingToken() {
        WorkflowAccessGuard guard = new WorkflowAccessGuard(true, "secret-token", localizedMessages());

        assertDoesNotThrow(() -> guard.verifyReadAccess("secret-token"));
    }

    @Test
    void verifyReadAccessRejectsMissingOrWrongToken() {
        WorkflowAccessGuard guard = new WorkflowAccessGuard(true, "secret-token", localizedMessages());

        assertThrows(ResponseStatusException.class, () -> guard.verifyReadAccess(null));
        assertThrows(ResponseStatusException.class, () -> guard.verifyReadAccess("wrong-token"));
    }

    private LocalizedMessages localizedMessages() {
        LocalizedMessages localizedMessages = mock(LocalizedMessages.class);
        when(localizedMessages.get("errors.request.forbidden"))
                .thenReturn("A valid workflow access token is required in this environment.");
        return localizedMessages;
    }
}
