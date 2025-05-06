package br.com.soejin.framework.security_guard.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of user role types in the system.
 * These roles are used for authorization and access control.
 * 
 * @see br.com.soejin.framework.security_guard.model.User#getRoles()
 * @see br.com.soejin.framework.security_guard.service.UserService#updateRole(Long, RoleTypeEnum)
 */
@AllArgsConstructor
@Getter
public enum RoleTypeEnum {
    /**
     * Administrator role with full system access
     */
    ROLE_ADMIN("ROLE_ADMIN"),

    /**
     * Standard user role with limited access
     */
    ROLE_USER("ROLE_USER");

    /**
     * The string representation of the role
     */
    private final String role;
}
