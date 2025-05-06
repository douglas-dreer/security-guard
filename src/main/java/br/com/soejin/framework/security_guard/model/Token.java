package br.com.soejin.framework.security_guard.model;

import br.com.soejin.framework.security_guard.enums.TokenTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TokenTypeEnum tokenType = TokenTypeEnum.BEARER;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    private LocalDateTime expirationDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;
}
