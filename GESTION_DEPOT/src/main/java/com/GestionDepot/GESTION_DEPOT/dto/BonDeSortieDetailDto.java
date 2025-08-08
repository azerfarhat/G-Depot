package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.enums.StatutBonDeSortie;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BonDeSortieDetailDto {
    private Long id;
    private String numeroBDS;
    private LocalDate dateSortie;
    private StatutBonDeSortie statut;
    private BigDecimal valeurTotaleInitialeTTC;
    private String chauffeurNom;
    private Long commandeOrigineId;
    private List<LigneBdsDetailDto> lignes; // Détails des lignes du bon de sortie
    private List<FactureSimpleDto> factures; // Détails des factures liées
}