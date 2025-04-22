package com.parkez.user.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmailAndRoleAndLoginType(String email, UserRole role, LoginType loginType);

	Optional<User> findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(String email, UserRole role, LoginType loginType);

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	Optional<User> findByEmailAndRoleAndDeletedAtIsNull(String email, UserRole role);

    List<User> findAllByRole(UserRole role);
}
