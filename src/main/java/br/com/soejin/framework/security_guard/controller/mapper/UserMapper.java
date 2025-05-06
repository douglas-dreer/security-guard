package br.com.soejin.framework.security_guard.controller.mapper;

import br.com.soejin.framework.security_guard.controller.request.CreateUserRequest;
import br.com.soejin.framework.security_guard.controller.response.UserResponse;
import br.com.soejin.framework.security_guard.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    UserResponse toResponse(User user);

    @Named("userToUserResponse")
    default UserResponse userToUserResponse(User user) {
        return toResponse(user);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetExpires", ignore = true)
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);

}
