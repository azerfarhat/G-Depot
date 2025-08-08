package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.Model.Produit;
import lombok.Data;

@Data
public class ProduitDTO {
    private Long id;
    private String nom;
    private String description;

    // Constructeur pour mapper l'entité Produit vers ce DTO
    public ProduitDTO(Produit produit) {
        this.id = produit.getId();
        this.nom = produit.getNom();
        this.description = produit.getDescription();
    }
}