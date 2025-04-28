package com.parkez.user.domain.repository;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserQueryDslRepository {

	boolean existsByEmailAndRoleAndLoginType(String email, UserRole role, LoginType loginType);

	Optional<User> findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(String email, UserRole role, LoginType loginType);

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	Optional<User> findByEmailAndRoleAndDeletedAtIsNull(String email, UserRole role);
}
