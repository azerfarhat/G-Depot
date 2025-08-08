package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Repository.UtilisateurRepository;
import com.GestionDepot.GESTION_DEPOT.Request.AuthRequestDto;
import com.GestionDepot.GESTION_DEPOT.Response.AuthResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository repository;
    private final JwtService jwtService;

    public AuthResponseDto login(AuthRequestDto request) {
        authenticationManager.authenticate( //authentication manager (providermanager) c'est l'ensemble des provider pour connecter le client ici le authenitication manager c'est le DaoAuthenticationProvider
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponseDto.builder().token(jwtToken).build(); //Le frontend va recevoir ce token et le stocker
    }
}