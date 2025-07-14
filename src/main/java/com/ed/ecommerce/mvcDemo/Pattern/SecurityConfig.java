
// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Pattern/SecurityConfig.java
package com.ed.ecommerce.mvcDemo.Pattern;

import com.ed.ecommerce.mvcDemo.Repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return correo -> usuarioRepository.findByCorreo(correo)
                .map(usuario -> org.springframework.security.core.userdetails.User.builder()
                        .username(usuario.getCorreo())
                        .password(usuario.getContrasena())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/img/**"),
                                new AntPathRequestMatcher("/icons/**")
                        ).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/index"),
                                new AntPathRequestMatcher("/ficcion"),
                                new AntPathRequestMatcher("/no-ficcion"),
                                new AntPathRequestMatcher("/infantil-juvenil"),
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/registro")
                        ).permitAll()
                        // ¡IMPORTANTE! Permite que cualquiera acceda a la API de ventas.
                        // Para producción, esto debería ser .authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/ventas/procesar")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/pagos/notificacion")).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // --- INICIO DE MODIFICACIÓN PARA LOGIN ---
                        .successHandler((request, response, authentication) -> {
                            String nombre = authentication.getName(); // Obtiene el correo
                            request.getSession().setAttribute("alertMessage", "¡Bienvenido, " + nombre + "!");
                            response.sendRedirect("/"); // Redirige a la página principal
                        })
                        // --- FIN DE MODIFICACIÓN ---
                        .failureUrl("/login?error") // Cambiado a solo 'error'
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}