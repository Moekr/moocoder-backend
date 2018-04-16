package com.moekr.aes.web.security.impl;

import com.moekr.aes.data.entity.User;
import com.moekr.aes.web.security.WebSecurityConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Set;

@Data
@EqualsAndHashCode
@ToString
public class CustomUserDetails implements UserDetails {
	private Integer id;
	private String username;
	private String password;
	private Set<GrantedAuthority> authorities;

	CustomUserDetails(User user) {
		BeanUtils.copyProperties(user, this);
		switch (user.getRole()) {
			case STUDENT:
				this.authorities = Collections.singleton(WebSecurityConfiguration.STUDENT_AUTHORITY);
				break;
			case TEACHER:
				this.authorities = Collections.singleton(WebSecurityConfiguration.TEACHER_AUTHORITY);
				break;
			default:
				throw new UsernameNotFoundException(username);
		}
	}

	CustomUserDetails(String username, String password) {
		this.username = username;
		this.password = DigestUtils.sha256Hex(password);
		this.authorities = Collections.singleton(WebSecurityConfiguration.ADMIN_AUTHORITY);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public boolean isStudent() {
		return authorities.contains(WebSecurityConfiguration.STUDENT_AUTHORITY);
	}

	public boolean isTeacher() {
		return authorities.contains(WebSecurityConfiguration.TEACHER_AUTHORITY);
	}

	public boolean isAdmin() {
		return authorities.contains(WebSecurityConfiguration.ADMIN_AUTHORITY);
	}
}
