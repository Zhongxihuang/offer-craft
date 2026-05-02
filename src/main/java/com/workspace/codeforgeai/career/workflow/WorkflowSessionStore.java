package com.workspace.codeforgeai.career.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkflowSessionStore {

    private final WorkflowSessionRepository workflowSessionRepository;
    private final ObjectMapper objectMapper;
    private final LocalizedMessages localizedMessages;
    private final Map<String, CareerWorkflowResponse> responseCache = new ConcurrentHashMap<>();

    public WorkflowSessionStore(WorkflowSessionRepository workflowSessionRepository,
                                ObjectMapper objectMapper,
                                LocalizedMessages localizedMessages) {
        this.workflowSessionRepository = workflowSessionRepository;
        this.objectMapper = objectMapper;
        this.localizedMessages = localizedMessages;
    }

    @Transactional
    public CareerWorkflowResponse save(CareerWorkflowResponse response) {
        return save(WorkflowPersistenceRecord.fromResponseOnly(response));
    }

    @Transactional
    public CareerWorkflowResponse save(WorkflowPersistenceRecord record) {
        if (record == null || record.response() == null || isBlank(record.response().workflowId())) {
            throw new IllegalArgumentException(localizedMessages.get("errors.workflow.response.required"));
        }

        WorkflowSessionEntity entity = workflowSessionRepository.findById(record.response().workflowId())
                .orElseGet(WorkflowSessionEntity::new);

        entity.setWorkflowId(record.response().workflowId());
        entity.setRootWorkflowId(normalizeWorkflowId(record.rootWorkflowId(), record.response().workflowId()));
        entity.setParentWorkflowId(trimToNull(record.parentWorkflowId()));
        entity.setWorkflowVersion(record.workflowVersion() == null || record.workflowVersion() < 1 ? 1 : record.workflowVersion());
        entity.setVersionReason(trimToNull(record.versionReason()));
        entity.setMemoryId(record.memoryId());
        entity.setContentLocale(normalizeLocale(record.response().contentLocale()));
        entity.setTargetRole(record.targetRole());
        entity.setTargetLevel(record.targetLevel());
        entity.setCompanyName(record.companyName());
        entity.setIncludeCompanyResearch(record.includeCompanyResearch());
        entity.setFocusAreasJson(writeJson(record.focusAreas()));
        entity.setJobDescriptionSourceType(resolveSourceType(record.jobDescription()));
        entity.setCandidateProfileSourceType(resolveSourceType(record.candidateProfile()));
        entity.setJobDescriptionOriginalFilename(record.jobDescription().originalFilename());
        entity.setCandidateProfileOriginalFilename(record.candidateProfile().originalFilename());
        entity.setJobDescriptionStoredPath(record.jobDescription().storedPath());
        entity.setCandidateProfileStoredPath(record.candidateProfile().storedPath());
        entity.setJobDescriptionText(record.jobDescription().text());
        entity.setCandidateProfileText(record.candidateProfile().text());
        entity.setWorkflowResponseJson(writeJson(record.response()));

        workflowSessionRepository.save(entity);
        responseCache.put(record.response().workflowId(), record.response());
        return record.response();
    }

    @Transactional(readOnly = true)
    public Optional<CareerWorkflowResponse> find(String workflowId) {
        if (isBlank(workflowId)) {
            return Optional.empty();
        }

        String normalizedWorkflowId = workflowId.trim();
        CareerWorkflowResponse cachedResponse = responseCache.get(normalizedWorkflowId);
        if (cachedResponse != null) {
            return Optional.of(cachedResponse);
        }

        return workflowSessionRepository.findById(normalizedWorkflowId)
                .map(this::readResponse)
                .map(response -> {
                    responseCache.put(normalizedWorkflowId, response);
                    return response;
                });
    }

    @Transactional(readOnly = true)
    public Optional<StoredWorkflowContext> findStoredContext(String workflowId) {
        if (isBlank(workflowId)) {
            return Optional.empty();
        }

        return workflowSessionRepository.findById(workflowId.trim())
                .map(entity -> new StoredWorkflowContext(
                        readResponse(entity),
                        entity.getMemoryId(),
                        entity.getTargetRole(),
                        entity.getTargetLevel(),
                        entity.getCompanyName(),
                        entity.isIncludeCompanyResearch(),
                        readFocusAreas(entity.getFocusAreasJson()),
                        entity.getJobDescriptionText(),
                        entity.getCandidateProfileText(),
                        normalizeLocale(entity.getContentLocale()),
                        buildVersionInfo(entity, readResponse(entity))
                ));
    }

    @Transactional(readOnly = true)
    public List<WorkflowVersionSummary> listVersions(String workflowId) {
        if (isBlank(workflowId)) {
            return List.of();
        }

        WorkflowSessionEntity currentEntity = workflowSessionRepository.findById(workflowId.trim()).orElse(null);
        if (currentEntity == null) {
            return List.of();
        }

        String rootWorkflowId = normalizeWorkflowId(currentEntity.getRootWorkflowId(), currentEntity.getWorkflowId());
        List<WorkflowSessionEntity> entities = workflowSessionRepository.findVersionHistory(rootWorkflowId);

        if (entities.isEmpty()) {
            entities = List.of(currentEntity);
        }

        return entities.stream()
                .map(entity -> {
                    CareerWorkflowResponse response = readResponse(entity);
                    WorkflowVersionInfo versionInfo = buildVersionInfo(entity, response);
                    return new WorkflowVersionSummary(
                            response.workflowId(),
                            versionInfo.rootWorkflowId(),
                            versionInfo.parentWorkflowId(),
                            versionInfo.versionNumber(),
                            response.generatedAt(),
                            response.decisionSummary() == null ? null : response.decisionSummary().fitLevel(),
                            normalizeLocale(response.contentLocale()),
                            versionInfo.versionReason()
                    );
                })
                .toList();
    }

    private CareerWorkflowResponse readResponse(WorkflowSessionEntity entity) {
        try {
            CareerWorkflowResponse response = objectMapper.readValue(entity.getWorkflowResponseJson(), CareerWorkflowResponse.class);
            String normalizedLocale = !isBlank(response.contentLocale())
                    ? normalizeLocale(response.contentLocale())
                    : normalizeLocale(entity.getContentLocale());
            WorkflowVersionInfo versionInfo = response.versionInfo() != null
                    ? response.versionInfo()
                    : buildVersionInfo(entity, response);

            return new CareerWorkflowResponse(
                    response.workflowId(),
                    response.generatedAt(),
                    normalizedLocale,
                    versionInfo,
                    response.decisionSummary(),
                    response.confidenceSummary(),
                    response.decisionDrivers() == null ? List.of() : response.decisionDrivers(),
                    response.clarificationQuestions() == null ? List.of() : response.clarificationQuestions(),
                    response.jdAnalysis(),
                    response.candidateAnalysis(),
                    response.gapAnalysis(),
                    response.interviewPrep(),
                    response.usedSearch(),
                    response.usedRetrieval(),
                    response.degradedMode(),
                    response.degradationNotes() == null ? List.of() : response.degradationNotes(),
                    response.actionPlan() == null ? List.of() : response.actionPlan(),
                    response.nextSteps(),
                    response.supportCapabilities()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(localizedMessages.get("errors.workflow.response.readFailed"), exception);
        }
    }

    private WorkflowVersionInfo buildVersionInfo(WorkflowSessionEntity entity, CareerWorkflowResponse response) {
        String rootWorkflowId = normalizeWorkflowId(entity.getRootWorkflowId(), response.workflowId());
        int versionNumber = entity.getWorkflowVersion() == null || entity.getWorkflowVersion() < 1 ? 1 : entity.getWorkflowVersion();
        return new WorkflowVersionInfo(
                rootWorkflowId,
                trimToNull(entity.getParentWorkflowId()),
                versionNumber,
                trimToNull(entity.getVersionReason())
        );
    }

    private List<String> readFocusAreas(String value) {
        if (isBlank(value)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(localizedMessages.get("errors.workflow.response.readFailed"), exception);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(localizedMessages.get("errors.workflow.response.writeFailed"), exception);
        }
    }

    private String resolveSourceType(WorkflowDocumentInput documentInput) {
        if (documentInput == null || documentInput.sourceType() == null) {
            return WorkflowSourceType.UNKNOWN.name();
        }
        return documentInput.sourceType().name();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeLocale(String locale) {
        return SupportedLocale.from(locale).languageTag();
    }

    private String normalizeWorkflowId(String workflowId, String fallback) {
        String normalized = trimToNull(workflowId);
        return normalized == null ? fallback : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
