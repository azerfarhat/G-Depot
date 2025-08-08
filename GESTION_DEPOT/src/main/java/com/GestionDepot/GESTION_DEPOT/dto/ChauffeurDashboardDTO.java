package com.GestionDepot.GESTION_DEPOT.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChauffeurDashboardDTO {
    private ChauffeurInfoDTO chauffeur;
    private ChauffeurStatsDTO stats; // Renommé pour éviter le conflit
    private List<HistoriqueBdsDTO> historiqueBons;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ChauffeurInfoDTO {
        private Long id;
        private String nom;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChauffeurStatsDTO { // Renommé ici
        private BigDecimal totalPrix; // Corrigé de totalPris à totalPrix
        private BigDecimal totalVendu;
        private BigDecimal totalRetourne;
        private BigDecimal margeBeneficiaire;
        private double tauxDeVente;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HistoriqueBdsDTO {
        private String numeroBDS;
        private LocalDate dateSortie;
        private BigDecimal valeurInitiale;
        private BigDecimal valeurVendue;
        private BigDecimal valeurRetournee;
        private BigDecimal marge;
        private String statut;
    }
}