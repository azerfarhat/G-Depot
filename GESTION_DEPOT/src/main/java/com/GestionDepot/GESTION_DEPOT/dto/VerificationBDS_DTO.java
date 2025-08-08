package com.GestionDepot.GESTION_DEPOT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationBDS_DTO {
    private Long bdsId;
    private String numeroBDS;
    private BigDecimal valeurTotaleInitiale;
    private BigDecimal totalVendu;
    private BigDecimal valeurRetour;
    private BigDecimal ecart;
    private String statutCoherence; // EX: "COHERENT", "INCOHERENT"
}