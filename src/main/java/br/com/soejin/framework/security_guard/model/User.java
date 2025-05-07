package br.com.soejin.framework.security_guard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classe de entidade que representa um usuário no sistema.
 * Esta classe implementa a interface UserDetails para integração com o Spring Security.
 * Contém todas as informações relacionadas ao usuário, incluindo detalhes de autenticação e autorização.
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see org.springframework.security.core.GrantedAuthority
 */
@Entity
@Table(name = "users")
@AllArgsConstructor
@Data
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @Column(name = "enabled")
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    public User() {
        this.roles = new HashSet<>();
        addRole("ROLE_USER");
    }

    /**
     * Retorna as autoridades concedidas ao usuário.
     * Converte as roles do usuário em objetos GrantedAuthority do Spring Security.
     * Utiliza programação funcional para maior legibilidade e manutenção.
     *
     * @return Uma coleção de objetos GrantedAuthority representando as roles do usuário
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Retorna a senha usada para autenticar o usuário.
     *
     * @return A senha do usuário
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Define uma nova senha para o usuário e atualiza o timestamp de modificação.
     *
     * @param password A nova senha a ser definida
     */
    public void setPassword(String password) {
        this.password = password;
        updateTimestamp();
    }

    /**
     * Retorna o nome de usuário usado para autenticar o usuário.
     *
     * @return O nome de usuário
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indica se a conta do usuário expirou.
     *
     * @return true se a conta do usuário for válida (não expirada), false caso contrário
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Indica se o usuário está bloqueado ou desbloqueado.
     *
     * @return true se o usuário não estiver bloqueado, false caso contrário
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Indica se as credenciais do usuário (senha) expiraram.
     *
     * @return true se as credenciais do usuário forem válidas (não expiradas), false caso contrário
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * Indica se o usuário está habilitado ou desabilitado.
     *
     * @return true se o usuário estiver habilitado, false caso contrário
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Adiciona uma role ao conjunto de roles do usuário.
     * 
     * @param role A role a ser adicionada
     * @return true se a role foi adicionada, false se já existia
     */
    public boolean addRole(String role) {
        boolean added = roles.add(role);
        if (added) {
            updateTimestamp();
        }
        return added;
    }
    
    /**
     * Remove uma role do conjunto de roles do usuário.
     * 
     * @param role A role a ser removida
     * @return true se a role foi removida, false se não existia
     */
    public boolean removeRole(String role) {
        boolean removed = roles.remove(role);
        if (removed) {
            updateTimestamp();
        }
        return removed;
    }
    
    /**
     * Retorna uma cópia imutável do conjunto de roles.
     * Isso previne modificação externa direta da coleção de roles.
     * 
     * @return Um conjunto imutável de roles
     */
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Gera um token de redefinição de senha para o usuário com um tempo de expiração.
     * Atualiza o timestamp de modificação.
     *
     * @param token O token de redefinição de senha a ser definido
     * @param expiryTime O tempo de expiração para o token de redefinição de senha
     */
    public void generatePasswordResetToken(String token, LocalDateTime expiryTime) {
        this.passwordResetToken = token;
        this.passwordResetExpires = expiryTime;
        updateTimestamp();
    }

    /**
     * Limpa o token de redefinição de senha e seu tempo de expiração.
     * Atualiza o timestamp de modificação.
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetExpires = null;
        updateTimestamp();
    }

    /**
     * Atualiza o timestamp do último login para o momento atual.
     * Também atualiza o timestamp de modificação.
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
        updateTimestamp();
    }
    
    /**
     * Método utilitário para atualizar o timestamp de modificação.
     * Centraliza a lógica de atualização do timestamp para evitar duplicação.
     */
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se um token de redefinição de senha é válido.
     * 
     * @param token O token a ser verificado
     * @return true se o token é válido e não expirou, false caso contrário
     */
    public boolean isPasswordResetTokenValid(String token) {
        return token != null && 
               token.equals(this.passwordResetToken) && 
               this.passwordResetExpires != null && 
               LocalDateTime.now().isBefore(this.passwordResetExpires);
    }
}
