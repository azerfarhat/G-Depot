package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.Utilisateur;
import com.GestionDepot.GESTION_DEPOT.dto.ChauffeurListDto;
import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    boolean existsByEmail(String email);
    List<Utilisateur> findByRole(RoleUtilisateur role);
    Optional<Utilisateur> findByEmail(String email);

    // ==========================================================================================
    // === MÉTHODE findAllChauffeursDto OPTIMISÉE AVEC LEFT JOIN POUR GÉRER LES VÉHICULES NULL ===
    // ==========================================================================================
    @Query("SELECT NEW com.GestionDepot.GESTION_DEPOT.dto.ChauffeurListDto(" +
            "u.id, " +
            "u.nom, " +
            "u.email, " +
            "u.telephone, " +
            "u.numeroPermis, " +
            "COALESCE(CONCAT(v.marque, ' ', v.modele, ' (', v.matricule, ')'), 'Non assigné'), " + // Référence 'v' du LEFT JOIN
            "CAST(RAND() * 100 AS long)) " +
            "FROM Utilisateur u LEFT JOIN u.vehicule v WHERE u.role = 'CHAUFFEUR'") // <<< AJOUT DU LEFT JOIN u.vehicule v
    List<ChauffeurListDto> findAllChauffeursDto();

    // Méthode de recherche si vous l'utilisez
    // Appliquez la même logique de LEFT JOIN ici si searchChauffeursDto ne renvoie pas tous les chauffeurs
    @Query("SELECT new com.GestionDepot.GESTION_DEPOT.dto.ChauffeurListDto(" +
            "u.id, u.nom, u.email, u.telephone, u.numeroPermis, " +
            "COALESCE(CONCAT(v.marque, ' ', v.modele, ' (', v.matricule, ')'), 'Non assigné')) " +
            "FROM Utilisateur u LEFT JOIN u.vehicule v " + // <<< AJOUT DU LEFT JOIN u.vehicule v
            "WHERE u.role = 'CHAUFFEUR' AND " +
            "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :terme, '%')))")
    List<ChauffeurListDto> searchChauffeursDto(@Param("terme") String terme);
}