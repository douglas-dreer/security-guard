package br.com.soejin.framework.security_guard.service;


import br.com.soejin.framework.security_guard.model.User;

public interface UserService {
    User createUser(String username, String email, String password);
    User updatePassword(String username, String password);
    void deleteUser(String username);

    void updateLastLogin(User user);
}
