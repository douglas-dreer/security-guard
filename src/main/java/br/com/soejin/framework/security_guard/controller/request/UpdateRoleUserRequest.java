package br.com.soejin.framework.security_guard.controller.request;

import br.com.soejin.framework.security_guard.enums.RoleTypeEnum;

public record UpdateRoleUserRequest(
        RoleTypeEnum role
) {
}
