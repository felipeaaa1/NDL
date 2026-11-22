package com.ndlcommerce.adapters.persistence.user;

import com.ndlcommerce.domain.UserType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "app_user", schema = "ecommerce")
// TODO: adicionar created by e updated by com o user que esta criando/editando os usuários
public class UserDataMapper implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true)
  private String login;

  private String password;

  @Column(name = "user_type")
  @Enumerated(EnumType.STRING)
  private UserType type;

  @Column(unique = true)
  private String email;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime creationTime;

  private UUID created_by;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updateTime;

  private UUID updated_by;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "account_non_locked", nullable = false)
  private boolean accountNonLocked = false;

  @Column(name = "account_non_expired", nullable = false)
  private boolean accountNonExpired = true;

  @Column(name = "credentials_non_expired", nullable = false)
  private boolean credentialsNonExpired = true;

  @Column(name = "is_email_valid", nullable = false)
  private boolean isEmailValidated = false;

  public UserDataMapper(
      String login, String email, UserType type, String password, UUID created_by) {
    this.login = login;
    this.email = email;
    this.type = type;
    this.password = password;
    this.created_by = created_by;
  }

  public UserDataMapper(String login, String email, UserType type) {
    this.login = login;
    this.email = email;
    this.type = type;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + type.name()));
  }

  @Override
  public String getUsername() {
    return this.login;
  }

  @Override
  public boolean isAccountNonExpired() {
    return this.accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return this.credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled && this.accountNonExpired && this.accountNonLocked;
  }
}
