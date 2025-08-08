package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.BonDeSortie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BonDeSortieRepository extends JpaRepository<BonDeSortie, Long> {
    boolean existsByCommandeOrigineId(Long commandeId);

    List<BonDeSortie> findByChauffeurId(Long chauffeurId);
//    @Query("SELECT DISTINCT bds FROM BonDeSortie bds " +
//            "LEFT JOIN FETCH bds.commandeOrigine co " + // CORRECTION : 'commandeOrigine' et non 'commandes'
//            "LEFT JOIN FETCH bds.lignes l " +
//            "LEFT JOIN FETCH bds.factures f " +
//            "WHERE bds.id = :bdsId")
//    Optional<BonDeSortie> findByIdWithDetails(@Param("bdsId") Long bdsId);
}
