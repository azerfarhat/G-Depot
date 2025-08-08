package com.GestionDepot.GESTION_DEPOT.enums;

import lombok.Getter;

@Getter
public enum StatutStock {
    DISPONIBLE("Disponible", 1),
    ALERTE("En alerte", 2),
    RUPTURE("En rupture", 3),
    EXPIRE("Expiré", 4);

    private final String libelle;
    private final int priorite;

    StatutStock(String libelle, int priorite) {
        this.libelle = libelle;
        this.priorite = priorite;
    }
}