package com.workspace.codeforgeai.career.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkflowSessionRepository extends JpaRepository<WorkflowSessionEntity, String> {

    @Query("""
            select workflow
            from WorkflowSessionEntity workflow
            where workflow.rootWorkflowId = :rootWorkflowId
            order by workflow.workflowVersion asc, workflow.createdAt asc
            """)
    List<WorkflowSessionEntity> findVersionHistory(String rootWorkflowId);
}
