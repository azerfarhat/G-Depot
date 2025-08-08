package com.GestionDepot.GESTION_DEPOT.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LigneCommandeRequestDto {

    @NotNull(message = "L'ID du produit est obligatoire.")
    private Long produitId;

    @NotNull(message = "La quantité est obligatoire.")
    @Min(value = 1, message = "La quantité doit être d'au moins 1.")
    private Integer quantite;

    public Long getProduitId() {
        return produitId;
    }

    public Integer getQuantite() {
        return quantite;
    }
}