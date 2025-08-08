package com.GestionDepot.GESTION_DEPOT.dto;

public class FournisseurSummaryDto {
    private Long id;
    private String nom;
    private String societe;
    private String email;
    private String telephone;
    private int totalStocksQuantity; // Keep as 'int' for now
    private double totalCommandeValue;

    public FournisseurSummaryDto(
            Long id,
            String nom,
            String societe,
            String email,
            String telephone,
            int totalStocksQuantity, // Type must match the query's output
            double totalCommandeValue // Type must match the query's output
    ) {
        this.id = id;
        this.nom = nom;
        this.societe = societe;
        this.email = email;
        this.telephone = telephone;
        this.totalStocksQuantity = totalStocksQuantity;
        this.totalCommandeValue = totalCommandeValue;
    }

    // Getters must be present for Spring/Jackson to serialize the DTO to JSON
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getSociete() { return societe; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public int getTotalStocksQuantity() { return totalStocksQuantity; }
    public double getTotalCommandeValue() { return totalCommandeValue; }
}