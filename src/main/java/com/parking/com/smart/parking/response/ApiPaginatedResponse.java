package com.parking.com.smart.parking.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiPaginatedResponse {
    private String responseCode;
    private String responseMessage;
    private Object body;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    public ApiPaginatedResponse(String responseCode, String responseMessage, Object body, long totalElements, int totalPages, int currentPage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.body = body;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }
}
