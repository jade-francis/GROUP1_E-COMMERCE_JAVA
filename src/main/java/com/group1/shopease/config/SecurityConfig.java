 package com.group1.shopease.config;

  import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
  import org.springframework.security.crypto.password.PasswordEncoder;
  import org.springframework.security.authentication.AuthenticationManager;
  import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
  import org.springframework.http.HttpMethod;
  import org.springframework.security.web.SecurityFilterChain;
  import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
  import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

  @Configuration
  public class SecurityConfig {

      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http)
              throws Exception {

          http
              .authorizeHttpRequests(authorize -> authorize
                  .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                  .requestMatchers(
                      "/",
                      "/error",
                      "/products",
                      "/products/**",
                      "/cart",
                      "/cart/**",
                      "/login",
                      "/register",
                      "/forgot-password",
                      "/reset-password"
                  ).permitAll()
                  .requestMatchers("/uploads/**").permitAll()
                  .requestMatchers("/api/auth/seller-request").authenticated()
                  .requestMatchers("/api/auth/**").permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                  .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SELLER")
                  .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SELLER")
                  .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SELLER")
                  .requestMatchers("/api/orders/seller/**").hasRole("SELLER")
                  .requestMatchers("/api/admin/**").hasRole("ADMIN")
                  .requestMatchers("/admin/**").hasRole("ADMIN")
                  .anyRequest().authenticated()
              )
              .sessionManagement(session -> session
                  .sessionFixation().migrateSession()
              )
              .formLogin(form -> form
                  .loginPage("/login")
                  .loginProcessingUrl("/login")
                  .successHandler((request, response, authentication) -> {
                      boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                      response.sendRedirect(admin ? "/admin" : "/products");
                  })
                  .failureUrl("/login?error=true")
                  .usernameParameter("email")
                  .permitAll()
              )
              .logout(logout -> logout
                  .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                  .logoutSuccessUrl("/")
                  .permitAll()
              )
              .csrf(csrf -> csrf.disable());

          return http.build();
      }

      @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
      @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
  }
