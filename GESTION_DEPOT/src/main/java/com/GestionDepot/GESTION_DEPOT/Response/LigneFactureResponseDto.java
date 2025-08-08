// Exemple de LigneFactureResponseDto (créez-le si non existant)
package com.GestionDepot.GESTION_DEPOT.Response; // Ou votre package de DTOs

import java.math.BigDecimal;

public class LigneFactureResponseDto {
    private Long id;
    private Long produitId;
    private String produitNom; // Pour l'affichage dans le PDF
    private Integer quantite;
    private BigDecimal prixUnitaireTTC;
    private BigDecimal totalLigneTTC;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProduitId() { return produitId; }
    public void setProduitId(Long produitId) { this.produitId = produitId; }
    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public BigDecimal getPrixUnitaireTTC() { return prixUnitaireTTC; }
    public void setPrixUnitaireTTC(BigDecimal prixUnitaireTTC) { this.prixUnitaireTTC = prixUnitaireTTC; }
    public BigDecimal getTotalLigneTTC() { return totalLigneTTC; }
    public void setTotalLigneTTC(BigDecimal totalLigneTTC) { this.totalLigneTTC = totalLigneTTC; }
}