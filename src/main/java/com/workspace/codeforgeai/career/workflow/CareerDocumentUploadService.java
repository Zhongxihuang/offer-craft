package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.common.api.ApiErrorDetail;
import com.workspace.codeforgeai.common.api.ApiValidationException;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class CareerDocumentUploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "txt", "md");
    private static final Pattern SAFE_WORKFLOW_ID = Pattern.compile("[A-Za-z0-9_-]{1,128}");
    private static final int MAX_ORIGINAL_FILENAME_LENGTH = 120;

    private final Path uploadRoot;
    private final int maxExtractedTextChars;
    private final LocalizedMessages localizedMessages;

    public CareerDocumentUploadService(@Value("${career.workflow.storage.upload-root:./data/uploads}") String uploadRoot,
                                       @Value("${career.workflow.storage.max-extracted-text-chars:200000}") int maxExtractedTextChars,
                                       LocalizedMessages localizedMessages) {
        this.uploadRoot = Path.of(uploadRoot).toAbsolutePath().normalize();
        this.maxExtractedTextChars = Math.max(1, maxExtractedTextChars);
        this.localizedMessages = localizedMessages;
    }

    public WorkflowDocumentInput resolveDocument(String workflowId,
                                                 String fieldName,
                                                 MultipartFile file,
                                                 String textValue,
                                                 String locale) {
        SupportedLocale supportedLocale = SupportedLocale.from(locale);
        if (hasFile(file)) {
            return storeAndExtractFile(workflowId, fieldName, file, supportedLocale);
        }

        String normalizedText = normalizeText(textValue);
        if (normalizedText == null) {
            throw validationException(
                    fieldName,
                    localizedMessages.get(supportedLocale, "errors.upload.provideTextOrFile")
            );
        }
        validateExtractedTextLength(fieldName, normalizedText, supportedLocale);

        return WorkflowDocumentInput.text(normalizedText);
    }

    private WorkflowDocumentInput storeAndExtractFile(String workflowId,
                                                      String fieldName,
                                                      MultipartFile file,
                                                      SupportedLocale locale) {
        String originalFilename = normalizeFilename(file.getOriginalFilename());
        String extension = resolveExtension(originalFilename, fieldName, locale);
        Path targetPath = buildTargetPath(workflowId, fieldName, extension, locale);

        try {
            Files.createDirectories(targetPath.getParent());
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String extractedText = extractText(targetPath, extension);
            String normalizedText = normalizeText(extractedText);

            if (normalizedText == null) {
                Files.deleteIfExists(targetPath);
                throw validationException(
                        fieldName + "File",
                        extension.equals("pdf")
                                ? localizedMessages.get(locale, "errors.upload.unreadablePdf")
                                : localizedMessages.get(locale, "errors.upload.unreadableText")
                );
            }
            validateExtractedTextLength(fieldName + "File", normalizedText, locale);

            return WorkflowDocumentInput.file(originalFilename, targetPath.toString(), normalizedText);
        } catch (ApiValidationException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalStateException(localizedMessages.get(locale, "errors.upload.storeFailed"), exception);
        }
    }

    private String resolveExtension(String originalFilename, String fieldName, SupportedLocale locale) {
        if (originalFilename == null) {
            throw validationException(fieldName + "File", localizedMessages.get(locale, "errors.upload.unsupportedFileType"));
        }
        if (originalFilename.length() > MAX_ORIGINAL_FILENAME_LENGTH || containsControlCharacter(originalFilename)) {
            throw validationException(fieldName + "File", localizedMessages.get(locale, "errors.upload.invalidFilename"));
        }

        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == originalFilename.length() - 1) {
            throw validationException(fieldName + "File", localizedMessages.get(locale, "errors.upload.unsupportedFileType"));
        }

        String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw validationException(fieldName + "File", localizedMessages.get(locale, "errors.upload.unsupportedFileType"));
        }

        return extension;
    }

    private void validateExtractedTextLength(String fieldName, String text, SupportedLocale locale) {
        if (text.length() > maxExtractedTextChars) {
            throw validationException(
                    fieldName,
                    localizedMessages.get(locale, "errors.upload.textTooLong", maxExtractedTextChars)
            );
        }
    }

    private Path buildTargetPath(String workflowId, String fieldName, String extension, SupportedLocale locale) {
        if (workflowId == null || !SAFE_WORKFLOW_ID.matcher(workflowId).matches()) {
            throw validationException(fieldName + "File", localizedMessages.get(locale, "errors.upload.invalidWorkflowId"));
        }

        String baseName = fieldName
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase(Locale.ROOT);
        Path targetPath = uploadRoot.resolve(workflowId).resolve(baseName + "." + extension).normalize();
        if (!targetPath.startsWith(uploadRoot)) {
            throw validationException(fieldName + "File", localizedMessages.get(locale, "errors.upload.invalidPath"));
        }
        return targetPath;
    }

    private String extractText(Path filePath, String extension) throws IOException {
        return switch (extension) {
            case "pdf" -> extractPdfText(filePath);
            case "txt", "md" -> Files.readString(filePath, StandardCharsets.UTF_8);
            default -> throw new IllegalStateException("Unexpected extension: " + extension);
        };
    }

    private String extractPdfText(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String normalizeFilename(String originalFilename) {
        if (originalFilename == null) {
            return null;
        }

        String trimmed = originalFilename.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (containsControlCharacter(trimmed)) {
            return trimmed;
        }

        try {
            String normalized = Path.of(trimmed).getFileName().toString().trim();
            return normalized.isEmpty() ? null : normalized;
        } catch (InvalidPathException exception) {
            return trimmed;
        }
    }

    private boolean containsControlCharacter(String value) {
        return value.chars().anyMatch(character -> Character.isISOControl((char) character));
    }

    private String normalizeText(String textValue) {
        if (textValue == null) {
            return null;
        }

        String trimmed = textValue.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ApiValidationException validationException(String field, String message) {
        return new ApiValidationException(
                localizedMessages.get("errors.upload.validation"),
                List.of(new ApiErrorDetail(field, message))
        );
    }
}
