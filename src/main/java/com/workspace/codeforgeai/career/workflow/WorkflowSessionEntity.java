package com.workspace.codeforgeai.career.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "workflow_sessions")
public class WorkflowSessionEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 128)
    private String workflowId;

    @Column(length = 128)
    private String rootWorkflowId;

    @Column(length = 128)
    private String parentWorkflowId;

    private Integer workflowVersion;
    private String versionReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Integer memoryId;
    private String contentLocale;
    private String targetRole;
    private String targetLevel;
    private String companyName;
    private boolean includeCompanyResearch;

    @Lob
    private String focusAreasJson;

    private String jobDescriptionSourceType;
    private String candidateProfileSourceType;
    private String jobDescriptionOriginalFilename;
    private String candidateProfileOriginalFilename;
    private String jobDescriptionStoredPath;
    private String candidateProfileStoredPath;

    @Lob
    private String jobDescriptionText;

    @Lob
    private String candidateProfileText;

    @Lob
    @Column(nullable = false)
    private String workflowResponseJson;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getRootWorkflowId() {
        return rootWorkflowId;
    }

    public void setRootWorkflowId(String rootWorkflowId) {
        this.rootWorkflowId = rootWorkflowId;
    }

    public String getParentWorkflowId() {
        return parentWorkflowId;
    }

    public void setParentWorkflowId(String parentWorkflowId) {
        this.parentWorkflowId = parentWorkflowId;
    }

    public Integer getWorkflowVersion() {
        return workflowVersion;
    }

    public void setWorkflowVersion(Integer workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public String getVersionReason() {
        return versionReason;
    }

    public void setVersionReason(String versionReason) {
        this.versionReason = versionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(Integer memoryId) {
        this.memoryId = memoryId;
    }

    public String getContentLocale() {
        return contentLocale;
    }

    public void setContentLocale(String contentLocale) {
        this.contentLocale = contentLocale;
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

    public boolean isIncludeCompanyResearch() {
        return includeCompanyResearch;
    }

    public void setIncludeCompanyResearch(boolean includeCompanyResearch) {
        this.includeCompanyResearch = includeCompanyResearch;
    }

    public String getFocusAreasJson() {
        return focusAreasJson;
    }

    public void setFocusAreasJson(String focusAreasJson) {
        this.focusAreasJson = focusAreasJson;
    }

    public String getJobDescriptionSourceType() {
        return jobDescriptionSourceType;
    }

    public void setJobDescriptionSourceType(String jobDescriptionSourceType) {
        this.jobDescriptionSourceType = jobDescriptionSourceType;
    }

    public String getCandidateProfileSourceType() {
        return candidateProfileSourceType;
    }

    public void setCandidateProfileSourceType(String candidateProfileSourceType) {
        this.candidateProfileSourceType = candidateProfileSourceType;
    }

    public String getJobDescriptionOriginalFilename() {
        return jobDescriptionOriginalFilename;
    }

    public void setJobDescriptionOriginalFilename(String jobDescriptionOriginalFilename) {
        this.jobDescriptionOriginalFilename = jobDescriptionOriginalFilename;
    }

    public String getCandidateProfileOriginalFilename() {
        return candidateProfileOriginalFilename;
    }

    public void setCandidateProfileOriginalFilename(String candidateProfileOriginalFilename) {
        this.candidateProfileOriginalFilename = candidateProfileOriginalFilename;
    }

    public String getJobDescriptionStoredPath() {
        return jobDescriptionStoredPath;
    }

    public void setJobDescriptionStoredPath(String jobDescriptionStoredPath) {
        this.jobDescriptionStoredPath = jobDescriptionStoredPath;
    }

    public String getCandidateProfileStoredPath() {
        return candidateProfileStoredPath;
    }

    public void setCandidateProfileStoredPath(String candidateProfileStoredPath) {
        this.candidateProfileStoredPath = candidateProfileStoredPath;
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

    public String getWorkflowResponseJson() {
        return workflowResponseJson;
    }

    public void setWorkflowResponseJson(String workflowResponseJson) {
        this.workflowResponseJson = workflowResponseJson;
    }
}
