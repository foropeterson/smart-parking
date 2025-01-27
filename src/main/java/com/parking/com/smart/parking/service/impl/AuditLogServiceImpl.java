package com.parking.com.smart.parking.service.impl;

import com.parking.com.smart.parking.entities.AuditLog;
import com.parking.com.smart.parking.repository.AuditLogRepository;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void logAction(String entityName, Long entityId, String action, String details) {
        String currentUser = getCurrentUser();
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setUsername(currentUser);
        auditLog.setChangeTimestamp(LocalDateTime.now());
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }
    @Override
    public void logActionWithLogin(String username,String entityName, Long entityId, String action, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setUsername(username);
        auditLog.setChangeTimestamp(LocalDateTime.now());
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }


    public ApiResponse getAuditLogs() {
        List<AuditLog> auditLogPage = auditLogRepository.findAll();
        if (auditLogPage.isEmpty()) {
            return new ApiResponse("404", "Audit Log not found", null);

        }
        return new ApiResponse("200", "Audit Log fetched successfully", auditLogPage);
    }

    public ApiResponse getAuditLogsById(Long id) {
        Optional<AuditLog> auditLogPage = auditLogRepository.findById(id);
        if (!auditLogPage.isPresent()) {
            return new ApiResponse("404", "Audit Log not found", null);

        }
        return new ApiResponse("200", "Audit Log fetched successfully", auditLogPage);
    }

    private String getCurrentUser() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
