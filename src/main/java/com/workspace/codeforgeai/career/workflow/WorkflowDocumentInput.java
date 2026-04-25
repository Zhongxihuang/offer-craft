package com.workspace.codeforgeai.career.workflow;

public record WorkflowDocumentInput(
        WorkflowSourceType sourceType,
        String originalFilename,
        String storedPath,
        String text
) {

    public static WorkflowDocumentInput text(String text) {
        return new WorkflowDocumentInput(WorkflowSourceType.TEXT, null, null, normalize(text));
    }

    public static WorkflowDocumentInput file(String originalFilename, String storedPath, String text) {
        return new WorkflowDocumentInput(
                WorkflowSourceType.FILE,
                normalize(originalFilename),
                normalize(storedPath),
                normalize(text)
        );
    }

    public static WorkflowDocumentInput unknown() {
        return new WorkflowDocumentInput(WorkflowSourceType.UNKNOWN, null, null, null);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
