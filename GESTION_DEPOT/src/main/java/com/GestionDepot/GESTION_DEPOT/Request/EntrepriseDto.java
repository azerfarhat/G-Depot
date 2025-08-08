package com.GestionDepot.GESTION_DEPOT.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EntrepriseDto {

    @NotBlank(message = "Le nom ne peut pas être vide.")
    @Size(max = 100)
    private String nom;

    private String adresse;

    @Size(max = 20)
    private String telephone;

    @Email(message = "Format d'email invalide.")
    @Size(max = 100)
    private String email;

    @Size(max = 50)
    private String matriculeFiscal;

    // === NOUVEAU CHAMP AJOUTÉ ===
    @Size(max = 24, message = "Le RIB doit contenir 24 caractères.")
    private String rib;
}