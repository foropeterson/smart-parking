package com.parking.com.smart.parking.service;

import com.parking.com.smart.parking.response.ApiResponse;

public interface AuditLogService {
    void logAction(String entityName, Long entityId, String action, String details);
    ApiResponse getAuditLogs();
    ApiResponse getAuditLogsById(Long id);
    void logActionWithLogin(String username,String entityName, Long entityId, String action, String details);
}
