package com.GestionDepot.GESTION_DEPOT.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LigneBonDeSortieRequestDto {
    @NotNull
    private Long produitId;
    @Positive
    private int quantiteSortie; // Quantité de ce produit dans cette ligne du bon de sortie
    // Pas besoin de quantiteRetournee ici, elle est gérée après coup
}