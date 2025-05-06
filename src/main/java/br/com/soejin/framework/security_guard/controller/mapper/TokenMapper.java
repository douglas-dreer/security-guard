package br.com.soejin.framework.security_guard.controller.mapper;

import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.model.Token;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TokenMapper {
    TokenResponse toResponse(Token entity);
    Token toEntity(TokenResponse response);
}
