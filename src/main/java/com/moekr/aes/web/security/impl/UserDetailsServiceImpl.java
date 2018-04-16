package com.moekr.aes.web.security.impl;

import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
	private final SecurityProperties securityProperties;
	private final UserDAO userDAO;

	@Autowired
	public UserDetailsServiceImpl(SecurityProperties securityProperties, UserDAO userDAO) {
		this.securityProperties = securityProperties;
		this.userDAO = userDAO;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		if (username.equals(securityProperties.getUser().getName())) {
			return new CustomUserDetails(username, securityProperties.getUser().getPassword());
		} else {
			return loadNormalUserByUsername(username);
		}
	}

	private UserDetails loadNormalUserByUsername(String username) {
		User user = userDAO.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException(username);
		}
		return new CustomUserDetails(user);
	}
}
