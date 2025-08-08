package com.GestionDepot.GESTION_DEPOT.Request;

import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UtilisateurDto {
    // Champs obligatoires pour TOUS les utilisateurs
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    private String email;

    private String motDePasse;

    @NotNull(message = "Le rôle est obligatoire")
    private RoleUtilisateur role;

    // Champs optionnels pour TOUS les utilisateurs
    private String telephone;

    // Champs optionnels qui ne deviendront obligatoires que si le rôle est CHAUFFEUR
    private String numeroPermis;
    private String marqueVehicule;
    private String modeleVehicule;
    private String matriculeVehicule;
}