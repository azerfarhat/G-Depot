package com.GestionDepot.GESTION_DEPOT.Response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneBonDeSortieResponseDto {
    private Long id;
    private Long produitId;
    private String nomProduit; // Pour afficher le nom du produit
    private int quantiteSortie;
    private int quantiteRetournee;
    private int quantiteFacturee;
    private int quantiteDisponiblePourFacturation; // La quantité calculée !
    private BigDecimal prixUnitaireTTC;
}