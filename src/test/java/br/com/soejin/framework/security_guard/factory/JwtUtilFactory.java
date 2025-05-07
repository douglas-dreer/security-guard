package br.com.soejin.framework.security_guard.factory;

import br.com.soejin.framework.security_guard.model.User;
import br.com.soejin.framework.security_guard.util.JwtUtil;

public class JwtUtilFactory {
    private final User user = UserFactory.createUser();
    private final JwtUtil util = new JwtUtil();

}
