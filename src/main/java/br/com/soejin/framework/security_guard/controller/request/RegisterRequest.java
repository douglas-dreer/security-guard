package br.com.soejin.framework.security_guard.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "O campo nome é obrigatório")
    String name,
    @NotBlank(message = "O campo email é obrigatório")
    @Email(message = "O email fornecido não é válido")
    String email,
    @NotBlank(message = "O campo senha é obrigatório")
    String password
) {

}
