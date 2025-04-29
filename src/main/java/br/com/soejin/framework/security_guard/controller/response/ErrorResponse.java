package br.com.soejin.framework.security_guard.controller.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
    @Schema(description = "Código de status da resposta")
    int status,
    @Schema(description = "Mensagem de erro")
    String message,
    @Schema(description = "Data e hora do erro")
    LocalDateTime localdateTime)
    {

    }
