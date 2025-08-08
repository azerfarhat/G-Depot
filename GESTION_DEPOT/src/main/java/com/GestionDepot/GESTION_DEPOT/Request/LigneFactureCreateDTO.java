package com.GestionDepot.GESTION_DEPOT.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LigneFactureCreateDTO {

    @NotNull(message = "L'ID du produit ne peut pas être nul.")
    private Long produitId;

    @NotNull(message = "La quantité ne peut pas être nulle.")
    @Min(value = 1, message = "La quantité doit être d'au moins 1.")
    private Integer quantite;

}