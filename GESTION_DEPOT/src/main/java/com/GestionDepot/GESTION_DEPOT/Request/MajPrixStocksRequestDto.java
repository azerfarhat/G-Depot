package com.GestionDepot.GESTION_DEPOT.Request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MajPrixStocksRequestDto {
    private Long produitId;
    private BigDecimal nouveauPrixVenteHTVA;

    public void setNouveauPrixVenteHTVA(BigDecimal nouveauPrixVenteHTVA) {
        this.nouveauPrixVenteHTVA = nouveauPrixVenteHTVA;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }
}