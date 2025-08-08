// package com.GestionDepot.GESTION_DEPOT.Dto;
// MODIFIÉ
package com.GestionDepot.GESTION_DEPOT.Dto;

import com.GestionDepot.GESTION_DEPOT.Response.UtilisateurSimpleDto;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DepotDto {
    private Long id;
    private String nom;
    private String adresse;
    private String ville;        // RÉINTRODUIT
    private String codePostal;   // RÉINTRODUIT
    private String zone;         // RÉINTRODUIT
    private String telephone;
    private String email;        // RÉINTRODUIT
    private BigDecimal tva;
    private BigDecimal chiffreAffaires;
    private UtilisateurSimpleDto responsable;

    // Assurez-vous que votre constructeur ou méthode de conversion (convertToDepotDto)
    // gère ces nouveaux champs.
}