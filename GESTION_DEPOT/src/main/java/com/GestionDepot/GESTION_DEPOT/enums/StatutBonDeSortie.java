package com.GestionDepot.GESTION_DEPOT.enums;

public enum StatutBonDeSortie {
    CREE,
    LIVRE,
    PARTIELLEMENT_LIVRE, // Si une partie est livrée et une autre non
    ANNULE,
    FACTURE, // Nouveau statut : le bon de sortie est entièrement facturé
    PARTIELLEMENT_FACTURE,
    EN_COURS,    // Le chauffeur est en tournée
    RETOURNE,    // Le chauffeur est revenu, les retours sont enregistrés
    CLOTURE      // La vérification a été faite et le bon est archivé
}