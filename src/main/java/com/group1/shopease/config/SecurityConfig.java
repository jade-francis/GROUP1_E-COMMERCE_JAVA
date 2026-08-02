 package com.group1.shopease.config;

  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
  import org.springframework.security.crypto.password.PasswordEncoder;
  import org.springframework.security.authentication.AuthenticationManager;
  import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
  import org.springframework.http.HttpMethod;
  import org.springframework.security.web.SecurityFilterChain;

  @Configuration
  public class SecurityConfig {

      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http)
              throws Exception {

          http
              .authorizeHttpRequests(authorize -> authorize
                  .requestMatchers(
                      "/",
                      "/css/**",
                      "/js/**",
                      "/images/**",
                      "/login",
                      "/register"
                  ).permitAll()
                  .requestMatchers("/api/auth/seller-request").authenticated()
                  .requestMatchers("/api/auth/**").permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                  .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SELLER")
                  .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SELLER")
                  .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SELLER")
                  .requestMatchers("/api/orders/seller/**").hasRole("SELLER")
                  .requestMatchers("/api/admin/**").hasRole("ADMIN")
                  .requestMatchers("/admin/**").hasRole("ADMIN")
                  .anyRequest().authenticated()
              )
              .formLogin(form -> form
                  .loginPage("/login")
                  .defaultSuccessUrl("/", true)
                  .permitAll()
              )
              .logout(logout -> logout
                  .logoutSuccessUrl("/")
                  .permitAll()
              )
              .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

          return http.build();
      }

      @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
      @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
  }
