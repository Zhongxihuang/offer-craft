package com.workspace.codeforgeai.career.api;

import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

public class CareerWorkflowUploadRequest {

    private Integer memoryId;
    private String workflowId;
    private String locale;
    private String targetRole;
    private String targetLevel;
    private String companyName;
    private String focusAreas;
    private Boolean includeCompanyResearch;
    private String jobDescriptionText;
    private String candidateProfileText;
    private MultipartFile jobDescriptionFile;
    private MultipartFile candidateProfileFile;

    public Integer getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(Integer memoryId) {
        this.memoryId = memoryId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getFocusAreas() {
        return focusAreas;
    }

    public void setFocusAreas(String focusAreas) {
        this.focusAreas = focusAreas;
    }

    public Boolean getIncludeCompanyResearch() {
        return includeCompanyResearch;
    }

    public void setIncludeCompanyResearch(Boolean includeCompanyResearch) {
        this.includeCompanyResearch = includeCompanyResearch;
    }

    public String getJobDescriptionText() {
        return jobDescriptionText;
    }

    public void setJobDescriptionText(String jobDescriptionText) {
        this.jobDescriptionText = jobDescriptionText;
    }

    public String getCandidateProfileText() {
        return candidateProfileText;
    }

    public void setCandidateProfileText(String candidateProfileText) {
        this.candidateProfileText = candidateProfileText;
    }

    public MultipartFile getJobDescriptionFile() {
        return jobDescriptionFile;
    }

    public void setJobDescriptionFile(MultipartFile jobDescriptionFile) {
        this.jobDescriptionFile = jobDescriptionFile;
    }

    public MultipartFile getCandidateProfileFile() {
        return candidateProfileFile;
    }

    public void setCandidateProfileFile(MultipartFile candidateProfileFile) {
        this.candidateProfileFile = candidateProfileFile;
    }

    public List<String> normalizedFocusAreas() {
        if (focusAreas == null || focusAreas.isBlank()) {
            return Collections.emptyList();
        }

        return List.of(focusAreas.split(","))
                .stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    public boolean includeCompanyResearchEnabled() {
        return Boolean.TRUE.equals(includeCompanyResearch);
    }
}
