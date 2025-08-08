package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.Model.LigneFacture;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LigneFactureDTO {
    private Long id;
    private int quantite;
    private BigDecimal prixUnitaireTTC;
    private BigDecimal totalLigneTTC;
    private ProduitDTO produit; // Utilise le ProduitDTO

    // Constructeur pour mapper l'entité LigneFacture vers ce DTO
    public LigneFactureDTO(LigneFacture ligne) {
        this.id = ligne.getId();
        this.quantite = ligne.getQuantite();
        this.prixUnitaireTTC = ligne.getPrixUnitaireTTC();
        this.totalLigneTTC = ligne.getTotalLigneTTC();
        this.produit = new ProduitDTO(ligne.getProduit()); // Convertit aussi le produit associé
    }
}