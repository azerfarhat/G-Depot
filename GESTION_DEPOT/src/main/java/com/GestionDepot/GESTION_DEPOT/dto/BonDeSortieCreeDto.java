package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.Model.BonDeSortie;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BonDeSortieCreeDto {
    private Long id;
    private String numeroBDS;
    private LocalDate dateSortie;
    private Long commandeOrigineId;
    private Long chauffeurId;

    public BonDeSortieCreeDto(BonDeSortie bonDeSortie) {
        this.id = bonDeSortie.getId();
        this.numeroBDS = bonDeSortie.getNumeroBDS();
        this.dateSortie = bonDeSortie.getDateSortie();
        if (bonDeSortie.getCommandeOrigine() != null) {
            this.commandeOrigineId = bonDeSortie.getCommandeOrigine().getId();
        }
        if (bonDeSortie.getChauffeur() != null) {
            this.chauffeurId = bonDeSortie.getChauffeur().getId();
        }
    }
}