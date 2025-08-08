package com.GestionDepot.GESTION_DEPOT.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RetourProduitDTO {
    @NotNull
    private Long ligneBonDeSortieId;
    @Positive
    private int quantiteRetournee;
}