package com.moekr.moocoder.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public abstract class WebSecurityConstants {
	private static final String ROLE_PREFIX = "ROLE_";

	public static final String STUDENT_ROLE = "STUDENT";
	public static final String TEACHER_ROLE = "TEACHER";
	public static final String ADMIN_ROLE = "ADMIN";

	public static final GrantedAuthority STUDENT_AUTHORITY = new SimpleGrantedAuthority(ROLE_PREFIX + STUDENT_ROLE);
	public static final GrantedAuthority TEACHER_AUTHORITY = new SimpleGrantedAuthority(ROLE_PREFIX + TEACHER_ROLE);
	public static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority(ROLE_PREFIX + ADMIN_ROLE);
}
