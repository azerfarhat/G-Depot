package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.enums.RetreiveProductMethode;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProduitListDTO {
    private Long id;
    private String nom;
    private String categorie;
    private long stockTotal;
    private int stockMinimum;
    private long nombreLots;
    private BigDecimal dernierPrixVenteHTVA;
    private BigDecimal dernierPrixVenteTTC;
    private String description;
    private RetreiveProductMethode strategieStock;


    // === CONSTRUCTEUR MIS À JOUR pour inclure les nouveaux champs ===
    public ProduitListDTO(Long id, String nom, String categorie, long stockTotal, int stockMinimum, long nombreLots, BigDecimal dernierPrixVenteHTVA, BigDecimal dernierPrixVenteTTC, String description, RetreiveProductMethode strategieStock) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.stockTotal = stockTotal;
        this.stockMinimum = stockMinimum;
        this.nombreLots = nombreLots;
        this.dernierPrixVenteHTVA = dernierPrixVenteHTVA;
        this.dernierPrixVenteTTC = dernierPrixVenteTTC;
        this.description = description; // <-- Ajouté
        this.strategieStock = strategieStock; // <-- Ajouté
    }
}