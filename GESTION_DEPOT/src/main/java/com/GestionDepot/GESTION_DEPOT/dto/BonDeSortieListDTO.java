package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.Model.BonDeSortie;
import com.GestionDepot.GESTION_DEPOT.enums.StatutBonDeSortie;
import lombok.Data;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class BonDeSortieListDTO {
    private Long id;
    private String numeroBDS;
    private LocalDate dateSortie;
    private StatutBonDeSortie statut;
    private String chauffeurNom; // Pour la liste
    private BigDecimal valeurTotaleInitialeTTC;

    public static BonDeSortieListDTO fromEntity(BonDeSortie bonDeSortie) {
        BonDeSortieListDTO dto = new BonDeSortieListDTO();
        dto.setId(bonDeSortie.getId());
        dto.setNumeroBDS(bonDeSortie.getNumeroBDS());
        dto.setDateSortie(bonDeSortie.getDateSortie());
        dto.setStatut(bonDeSortie.getStatut());
        dto.setValeurTotaleInitialeTTC(bonDeSortie.getValeurTotaleInitialeTTC());
        if (bonDeSortie.getChauffeur() != null) {
            dto.setChauffeurNom(bonDeSortie.getChauffeur().getNom());
        }
        return dto;
    }
}