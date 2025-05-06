package br.com.soejin.framework.security_guard.controller.response;

import java.util.List;

public record PageResponse<T> (
        int page,
        int pageSize,
        long totalPages,
        long totalElements,
        List<T> content
        ) {
}
