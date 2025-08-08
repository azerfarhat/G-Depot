package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.Facture;
import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    // Méthode existante, mais pour récupérer l'entité complète
    Optional<Facture> findByCommandeId(Long commandeId);

    // Nouvelle méthode : Trouver une facture par l'ID de son Bon de Sortie
    Optional<Facture> findByBonDeSortieId(Long bonDeSortieId);

    // Nouvelle méthode : Trouver toutes les factures associées à un chauffeur (via BonDeSortie)
    // Nécessite que l'entité BonDeSortie ait une propriété 'chauffeur' de type Utilisateur.
    List<Facture> findByBonDeSortie_Chauffeur_Id(Long chauffeurId);

    // Méthode existante, mais pour info

    // Méthode existante, mais pour info

    boolean existsByCommandeId(Long commandeId);

    List<Facture> findByCommandeIdIn(List<Long> commandeIds);



    @Query("SELECT SUM(f.totalTTC) FROM Facture f WHERE f.statut = 'PAYEE' AND f.dateFacturation >= :debutMois")
    Optional<BigDecimal> findMonthlyRevenue(StatutFacture payee, @Param("debutMois") LocalDate debutMois);
}
