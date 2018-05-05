package com.moekr.moocoder.data.dao;

import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.util.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User, Integer> {
	User findById(int id);

	User findByUsername(String username);

	User findByEmail(String email);

	Page<User> findAllByUsernameLike(String search, Pageable pageable);

	Integer countByRole(UserRole role);
}
