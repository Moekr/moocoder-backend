package com.moekr.moocoder.web.security;

import com.moekr.moocoder.web.handler.LoginRedirectHandler;
import com.moekr.moocoder.web.security.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

import static com.moekr.moocoder.web.security.WebSecurityConstants.*;

@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class WebSecurityConfiguration {
	@Configuration
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public static class ApiWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**")
					.authorizeRequests()
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
					.antMatchers("/register.html", "/internal/**", "/webjars/**", "/js/**", "/css/**", "/favicon.ico").permitAll()
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
