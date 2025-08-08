package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.Model.Facture;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FactureDTO {
    private Long id;
    private String numeroFacture;
    private LocalDate dateFacturation;
    private String statut;
    private BigDecimal totalTTC;
    private List<LigneFactureDTO> lignes; // Utilise une liste de LigneFactureDTO

    // Constructeur pour mapper l'entité Facture vers ce DTO
    public FactureDTO(Facture facture) {
        this.id = facture.getId();
        this.numeroFacture = facture.getNumeroFacture();
        this.dateFacturation = facture.getDateFacturation();
        this.statut = facture.getStatut().name();
        this.totalTTC = facture.getTotalTTC();
        // C'est ici que la magie opère : on convertit chaque LigneFacture en LigneFactureDTO
        // pendant que la transaction est encore ouverte, ce qui permet de charger les produits.
        this.lignes = facture.getLignes().stream()
                .map(LigneFactureDTO::new)
                .collect(Collectors.toList());
    }
}