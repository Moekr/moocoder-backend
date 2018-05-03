package com.moekr.aes.web.security;

import com.moekr.aes.web.handler.LoginRedirectHandler;
import com.moekr.aes.web.security.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableWebSecurity
public class WebSecurityConfiguration {
	public static final GrantedAuthority STUDENT_AUTHORITY = new SimpleGrantedAuthority("STUDENT");
	public static final GrantedAuthority TEACHER_AUTHORITY = new SimpleGrantedAuthority("TEACHER");
	public static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ADMIN");

	@Configuration
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public static class ApiWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**")
					.authorizeRequests()
					.antMatchers(HttpMethod.POST, "/api/exam").hasAnyAuthority(TEACHER_AUTHORITY.getAuthority())
					.antMatchers(HttpMethod.POST, "/api/exam/*/join").hasAnyAuthority(TEACHER_AUTHORITY.getAuthority(), STUDENT_AUTHORITY.getAuthority())
					.antMatchers(HttpMethod.GET, "/api/user/current").authenticated()
					.antMatchers("/api/user", "/api/user/**").hasAuthority(ADMIN_AUTHORITY.getAuthority())
					.antMatchers(HttpMethod.POST).hasAnyAuthority(TEACHER_AUTHORITY.getAuthority(), ADMIN_AUTHORITY.getAuthority())
					.antMatchers(HttpMethod.PUT).hasAnyAuthority(TEACHER_AUTHORITY.getAuthority(), ADMIN_AUTHORITY.getAuthority())
					.antMatchers(HttpMethod.DELETE).hasAnyAuthority(TEACHER_AUTHORITY.getAuthority(), ADMIN_AUTHORITY.getAuthority())
					.anyRequest().authenticated()
					.and()
					.httpBasic()
					.and()
					.csrf().disable()
					.cors();
		}
	}

	@Configuration
	public static class DefaultWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
		private final UserDetailsService userDetailsService;
		private final LoginRedirectHandler loginRedirectHandler;

		@Autowired
		public DefaultWebSecurityConfiguration(UserDetailsServiceImpl userDetailsService, LoginRedirectHandler loginRedirectHandler) {
			this.userDetailsService = userDetailsService;
			this.loginRedirectHandler = loginRedirectHandler;
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests()
					.antMatchers("/register.html", "/internal/**", "/js/**", "/css/**", "/fonts/**", "/favicon.ico").permitAll()
					.antMatchers("/file/**").hasAnyAuthority(TEACHER_AUTHORITY.getAuthority(), ADMIN_AUTHORITY.getAuthority())
					.anyRequest().authenticated()
					.and()
					.formLogin().loginPage("/login.html").defaultSuccessUrl("/").failureHandler(loginRedirectHandler).permitAll()
					.and()
					.logout().logoutUrl("/logout.html").logoutSuccessHandler(loginRedirectHandler).permitAll()
					.and()
					.rememberMe().rememberMeParameter("remember").rememberMeCookieName("REMEMBER").userDetailsService(userDetailsService)
					.and()
					.csrf().disable();
		}
	}
}
