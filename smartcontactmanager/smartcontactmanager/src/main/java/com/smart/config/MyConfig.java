package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {
		
	@Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImp();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
			DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
			daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
			daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
			return daoAuthenticationProvider;
	}
	
	@Bean 
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	   @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return     http
              .authorizeHttpRequests(authorize -> authorize
                              .requestMatchers("/admin/**").hasRole("ADMIN")
                              .requestMatchers("/user/**").hasRole("USER")
                              .requestMatchers("/**").permitAll()
              )
              .formLogin(from->from
            		  .loginPage("/log")
            		  .loginProcessingUrl("/dologin")
            		  .defaultSuccessUrl("/user/index")
            		 
            		  .permitAll())
              
              
              .csrf(csrf -> csrf.disable())    
              .build();

	   }
	
	
	
	
}
