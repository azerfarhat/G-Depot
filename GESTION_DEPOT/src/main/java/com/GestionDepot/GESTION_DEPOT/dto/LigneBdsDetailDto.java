package com.GestionDepot.GESTION_DEPOT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneBdsDetailDto {
    private Long id;
    private Long produitId;
    private String nomProduit;
    private int quantiteSortie;
    private int quantiteRetournee;
    private int quantiteFacturee; // <--- Ajout pour le DTO de détail
    private int quantiteDisponiblePourFacturation; // <--- Ajout pour le DTO de détail
    private BigDecimal prixUnitaireTTC;
    private BigDecimal totalLigneNonRetourneeTTC; // Total pour la quantité sortie - retournée
}