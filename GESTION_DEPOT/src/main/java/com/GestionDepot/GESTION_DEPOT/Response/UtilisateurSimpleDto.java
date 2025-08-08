package com.GestionDepot.GESTION_DEPOT.Response;

import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurSimpleDto {
    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String numeroPermis;
    private VehiculeDto vehicule; // Utilisera le nouveau VehiculeDto
    private RoleUtilisateur role; // Utile pour la logique frontend si nécessaire
}