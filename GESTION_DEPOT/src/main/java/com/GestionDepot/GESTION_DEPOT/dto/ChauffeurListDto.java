package com.GestionDepot.GESTION_DEPOT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChauffeurListDto {
    private Long id;
    private String nom;          // Le nom complet de l'utilisateur
    private String email;
    private String telephone;
    private String permis;       // Le numéro de permis
    private String vehiculeInfo; // Infos formatées du véhicule
    private long livraisons;     // Nombre de livraisons (simulé ici)
    // Le champ 'statut' est déjà supprimé.

    // =========================================================================================
    // === CONSTRUCTEUR pour findAllChauffeursDto (7 arguments) - Généré par @AllArgsConstructor
    // public ChauffeurListDto(Long id, String nom, String email, String telephone, String permis, String vehiculeInfo, long livraisons) { ... }
    // =========================================================================================


    // =========================================================================================
    // === NOUVEAU CONSTRUCTEUR MANUEL pour searchChauffeursDto (6 arguments) ===
    // === C'est celui qui manquait et qui causait l'erreur. ===
    // =========================================================================================
    public ChauffeurListDto(
            Long id,
            String nom,
            String email,
            String telephone,
            String permis,
            String vehiculeInfo) { // <<< Seulement 6 arguments ici

        this.id = id;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.permis = permis;
        this.vehiculeInfo = vehiculeInfo;
        this.livraisons = 0; // Valeur par défaut, car non fournie par la requête de recherche
    }
}