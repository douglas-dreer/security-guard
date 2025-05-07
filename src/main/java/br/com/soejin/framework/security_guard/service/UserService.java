package br.com.soejin.framework.security_guard.service;

import br.com.soejin.framework.security_guard.enums.RoleTypeEnum;
import br.com.soejin.framework.security_guard.model.User;

/**
 * Interface that defines user management services.
 * This interface provides methods for creating, updating, and deleting users,
 * as well as managing login information.
 * 
 * @see br.com.soejin.framework.security_guard.model.User
 * @see br.com.soejin.framework.security_guard.service.impl.UserServiceImpl
 */
public interface UserService {

    /**
     * Creates a new user in the system.
     *
     * @param username Unique username for the user
     * @param email User's email address
     * @param password User's password
     * @return The created user
     * @throws br.com.soejin.framework.security_guard.exception.NotFoundException If username or email already exists
     * @see br.com.soejin.framework.security_guard.model.User
     */
    User createUser(String username, String email, String password);

    /**
     * Updates the password of an existing user.
     *
     * @param username Username of the user whose password is to be updated
     * @param password New password for the user
     * @return The user with the updated password
     * @throws br.com.soejin.framework.security_guard.exception.UserNotFoundException If the user is not found
     */
    User updatePassword(String username, String password);

    /**
     * Removes a user from the system.
     *
     * @param username Username of the user to be removed
     * @throws br.com.soejin.framework.security_guard.exception.UserNotFoundException If the user is not found
     */
    void deleteUser(String username);

    /**
     * Updates the last login record of the user.
     *
     * @param user User who has logged in
     * @see br.com.soejin.framework.security_guard.model.User#updateLastLogin()
     */
    void updateLastLogin(User user);

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to find
     * @return The user with the specified username
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException If the user is not found
     * @see br.com.soejin.framework.security_guard.model.User
     */
    User findByUsername(String username);

    /**
     * Updates the role of a user identified by their unique ID.
     *
     * @param userId The unique identifier of the user whose role is being updated
     * @param role The new role to assign to the user
     * @throws br.com.soejin.framework.security_guard.exception.UserNotFoundException If the user is not found
     * @see br.com.soejin.framework.security_guard.enums.RoleTypeEnum
     */
    void updateRole(Long userId, RoleTypeEnum role);
}
