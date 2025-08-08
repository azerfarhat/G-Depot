package com.GestionDepot.GESTION_DEPOT.Response;

import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class FactureResponseDto {
    private Long id;
    private String numeroFacture;
    private LocalDate dateFacturation;
    private LocalDate dateEcheance;
    private StatutFacture statut;
    private BigDecimal totalHT;
    private BigDecimal montantTVA;
    private BigDecimal totalTTC;
    private Long commandeId;
    private UtilisateurSimpleDto client; // Informations du client de la commande
    private UtilisateurSimpleDto chauffeur; // Peut être null
    // --- NOUVEAUX CHAMPS POUR LES BESOINS DE RECHERCHE/AFFICHAGE ---
    // Informations liées au Bon de Sortie (pour les factures de chauffeur)
    private Long bonDeSortieId;
    private List<LigneFactureResponseDto> lignes; // <-- AJOUTEZ CETTE PROPRIÉTÉ

    // Vous devrez vous assurer que votre entité BonDeSortie a bien un champ 'chauffeur' de type Utilisateur
    public List<LigneFactureResponseDto> getLignes() {
        return lignes;
    }
    public void setLignes(List<LigneFactureResponseDto> lignes) {
        this.lignes = lignes;
    }
}