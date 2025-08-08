package com.GestionDepot.GESTION_DEPOT.config;

import com.GestionDepot.GESTION_DEPOT.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UtilisateurRepository repository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
         return NoOpPasswordEncoder.getInstance(); // si je le echange en return new BCryptPasswordEncoder(); il me envoi erreur 403
    }

    @Bean
    public AuthenticationProvider authenticationProvider() { //Comment vérifier l'identité
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(); //enregistres DaoAuthenticationProvider dans le contexte Spring comme bean
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {//C'est le "patron" qui coordonne toute la vérification d'identité.
        return config.getAuthenticationManager(); // crée un AuthenticationManager avec tous les AuthenticationProvider disponibles dans le contexte existe plusieur par exemple
        //authentication with google facebook ... autre on utilise l DaoAuthenticationProvider authentication avec email+password
    }
}