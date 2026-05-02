package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.common.api.ApiValidationException;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CareerDocumentUploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void resolveDocumentReturnsNormalizedTextWhenNoFileIsProvided() {
        CareerDocumentUploadService service = uploadService();

        WorkflowDocumentInput result = service.resolveDocument(
                "workflow-123",
                "jobDescription",
                null,
                "  Senior AI PM for enterprise copilots.  ",
                "en"
        );

        assertEquals(WorkflowSourceType.TEXT, result.sourceType());
        assertEquals("Senior AI PM for enterprise copilots.", result.text());
    }

    @Test
    void resolveDocumentRejectsMissingTextAndFileSources() {
        CareerDocumentUploadService service = uploadService();

        ApiValidationException exception = assertThrows(
                ApiValidationException.class,
                () -> service.resolveDocument("workflow-123", "candidateProfile", null, "   ", "en")
        );

        assertEquals("Upload request validation failed.", exception.getMessage());
        assertEquals("candidateProfile", exception.details().getFirst().field());
        assertTrue(exception.details().getFirst().message().contains("Provide pasted text"));
    }

    @Test
    void resolveDocumentReadsUtf8TextFiles() {
        CareerDocumentUploadService service = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "jobDescriptionFile",
                "job-description.txt",
                "text/plain",
                "Enterprise AI PM with governance ownership.".getBytes(StandardCharsets.UTF_8)
        );

        WorkflowDocumentInput result = service.resolveDocument("workflow-123", "jobDescription", file, null, "en");

        assertEquals(WorkflowSourceType.FILE, result.sourceType());
        assertEquals("job-description.txt", result.originalFilename());
        assertTrue(result.text().contains("Enterprise AI PM"));
        assertTrue(Files.exists(Path.of(result.storedPath())));
    }

    @Test
    void resolveDocumentReadsTextBasedPdfFiles() throws Exception {
        CareerDocumentUploadService service = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "candidateProfileFile",
                "candidate-profile.pdf",
                "application/pdf",
                createPdfBytes("Candidate has product analytics and AI workflow experience.")
        );

        WorkflowDocumentInput result = service.resolveDocument("workflow-456", "candidateProfile", file, null, "en");

        assertEquals(WorkflowSourceType.FILE, result.sourceType());
        assertNotNull(result.storedPath());
        assertTrue(result.text().contains("AI workflow experience"));
    }

    @Test
    void resolveDocumentRejectsUnsupportedFileTypes() {
        CareerDocumentUploadService service = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "jobDescriptionFile",
                "job-description.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "binary".getBytes(StandardCharsets.UTF_8)
        );

        ApiValidationException exception = assertThrows(
                ApiValidationException.class,
                () -> service.resolveDocument("workflow-789", "jobDescription", file, null, "en")
        );

        assertEquals("Upload request validation failed.", exception.getMessage());
        assertEquals("jobDescriptionFile", exception.details().getFirst().field());
    }

    @Test
    void resolveDocumentRejectsUnsafeWorkflowIdsBeforeSavingFiles() {
        CareerDocumentUploadService service = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "jobDescriptionFile",
                "job-description.txt",
                "text/plain",
                "Enterprise AI PM role".getBytes(StandardCharsets.UTF_8)
        );

        ApiValidationException exception = assertThrows(
                ApiValidationException.class,
                () -> service.resolveDocument("../outside", "jobDescription", file, null, "en")
        );

        assertEquals("Upload request validation failed.", exception.getMessage());
        assertEquals("jobDescriptionFile", exception.details().getFirst().field());
        assertTrue(exception.details().getFirst().message().contains("workflowId"));
    }

    @Test
    void resolveDocumentRejectsBlankPdfTextWithoutOcrSupport() throws Exception {
        CareerDocumentUploadService service = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "jobDescriptionFile",
                "job-description.pdf",
                "application/pdf",
                createPdfBytes(null)
        );

        ApiValidationException exception = assertThrows(
                ApiValidationException.class,
                () -> service.resolveDocument("workflow-999", "jobDescription", file, null, "en")
        );

        assertEquals("jobDescriptionFile", exception.details().getFirst().field());
        assertTrue(exception.details().getFirst().message().contains("OCR is not supported in v1"));
    }

    @Test
    void resolveDocumentRejectsOverlongExtractedText() {
        CareerDocumentUploadService service = new CareerDocumentUploadService(tempDir.toString(), 10, localizedMessages());

        ApiValidationException exception = assertThrows(
                ApiValidationException.class,
                () -> service.resolveDocument("workflow-123", "jobDescription", null, "This text is too long.", "en")
        );

        assertEquals("jobDescription", exception.details().getFirst().field());
        assertTrue(exception.details().getFirst().message().contains("too long"));
    }

    @Test
    void resolveDocumentRejectsUnsafeOriginalFilenames() {
        CareerDocumentUploadService service = uploadService();
        MockMultipartFile file = new MockMultipartFile(
                "jobDescriptionFile",
                "bad\u0000name.txt",
                "text/plain",
                "Enterprise AI PM role".getBytes(StandardCharsets.UTF_8)
        );

        ApiValidationException exception = assertThrows(
                ApiValidationException.class,
                () -> service.resolveDocument("workflow-123", "jobDescription", file, null, "en")
        );

        assertEquals("jobDescriptionFile", exception.details().getFirst().field());
    }

    private CareerDocumentUploadService uploadService() {
        return new CareerDocumentUploadService(tempDir.toString(), 200_000, localizedMessages());
    }

    private byte[] createPdfBytes(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            if (text != null && !text.isBlank()) {
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    contentStream.newLineAtOffset(72, 720);
                    contentStream.showText(text);
                    contentStream.endText();
                }
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private LocalizedMessages localizedMessages() {
        LocalizedMessages localizedMessages = org.mockito.Mockito.mock(LocalizedMessages.class);
        org.mockito.Mockito.when(localizedMessages.get(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> switch ((String) invocation.getArgument(0)) {
                    case "errors.upload.validation" -> "Upload request validation failed.";
                    default -> (String) invocation.getArgument(0);
                });
        org.mockito.Mockito.when(localizedMessages.get(org.mockito.ArgumentMatchers.any(com.workspace.codeforgeai.common.i18n.SupportedLocale.class), org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> switch ((String) invocation.getArgument(1)) {
                    case "errors.upload.provideTextOrFile" -> "Provide pasted text or upload a .pdf, .txt, or .md file.";
                    case "errors.upload.unsupportedFileType" -> "Upload a .pdf, .txt, or .md file.";
                    case "errors.upload.invalidFilename" -> "Use a shorter file name without control characters.";
                    case "errors.upload.invalidWorkflowId" -> "workflowId may only contain letters, numbers, underscores, and hyphens, and must be at most 128 characters.";
                    case "errors.upload.invalidPath" -> "The upload storage path is invalid.";
                    case "errors.upload.unreadablePdf" -> "The uploaded PDF did not contain extractable text. OCR is not supported in v1.";
                    case "errors.upload.unreadableText" -> "The uploaded file did not contain readable text.";
                    case "errors.upload.textTooLong" -> "The extracted document text is too long.";
                    case "errors.upload.storeFailed" -> "Failed to store or parse the uploaded file.";
                    default -> (String) invocation.getArgument(1);
                });
        org.mockito.Mockito.when(localizedMessages.get(
                        org.mockito.ArgumentMatchers.any(com.workspace.codeforgeai.common.i18n.SupportedLocale.class),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any()
                ))
                .thenAnswer(invocation -> switch ((String) invocation.getArgument(1)) {
                    case "errors.upload.textTooLong" -> "The extracted document text is too long.";
                    default -> (String) invocation.getArgument(1);
                });
        return localizedMessages;
    }
}
