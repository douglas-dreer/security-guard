package br.com.soejin.framework.security_guard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "blacklist")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Blacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, unique = true, updatable = false)
    private String token;

    @Column(nullable = false, unique = true, updatable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "banned_at", updatable = false)
    private java.time.LocalDateTime bannedAt;

}
