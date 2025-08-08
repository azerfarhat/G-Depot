package com.GestionDepot.GESTION_DEPOT.Response;

import com.GestionDepot.GESTION_DEPOT.enums.StatutBonDeSortie;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List; // Utilise List pour l'ordre des lignes si nécessaire

@Data
public class BonDeSortieResponseDto {
    private Long id;
    private String numeroBDS;
    private LocalDate dateSortie;
    private StatutBonDeSortie statut;
    private BigDecimal valeurTotaleInitialeTTC;

    private UtilisateurSimpleDto chauffeur;
    private Long commandeOrigineId; // Juste l'ID de la commande, pas l'objet complet

    private List<LigneBonDeSortieResponseDto> lignes; // <--- Les détails des lignes
}