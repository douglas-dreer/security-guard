package br.com.soejin.framework.security_guard.controller.mapper;

import br.com.soejin.framework.security_guard.controller.response.PageResponse;
import br.com.soejin.framework.security_guard.controller.response.TokenFullResponse;
import br.com.soejin.framework.security_guard.controller.response.TokenResponse;
import br.com.soejin.framework.security_guard.controller.response.UserResponse;
import br.com.soejin.framework.security_guard.model.Token;
import org.mapstruct.MapMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TokenMapper {
    TokenResponse toResponse(Token entity);

    @Mapping(target = "token", source = "response.token")
    @Mapping(target = "refreshToken", source = "response.refreshToken")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "revoked", constant = "false")
    Token toEntity(TokenResponse response);

    @Mapping(target = "content", expression = "java(tokenPage.getContent().stream().map(this::toFullResponse).toList())")
    @Mapping(target = "page", source = "tokenPage.number")
    @Mapping(target = "pageSize", source = "tokenPage.size")
    @Mapping(target = "totalPages", source = "tokenPage.totalPages")
    @Mapping(target = "totalElements", source = "tokenPage.totalElements")
    PageResponse<TokenFullResponse> toPageResponse(Page<Token> tokenPage);

    @Mapping(target = "user", source = "user", qualifiedByName = "userToUserResponse")
    TokenFullResponse toFullResponse(Token token);
}
