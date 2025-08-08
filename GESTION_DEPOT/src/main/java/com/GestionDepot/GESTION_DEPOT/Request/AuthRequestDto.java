package com.GestionDepot.GESTION_DEPOT.Request;
import lombok.Data;

@Data
public class AuthRequestDto {
    private String email;
    private String motDePasse;
}