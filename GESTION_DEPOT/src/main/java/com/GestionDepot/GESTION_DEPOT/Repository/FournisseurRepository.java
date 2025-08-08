package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.Fournisseur;
import com.GestionDepot.GESTION_DEPOT.Model.Stock;
import com.GestionDepot.GESTION_DEPOT.dto.FournisseurSummaryDto; // Import the new DTO
import com.GestionDepot.GESTION_DEPOT.dto.TopSupplierDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    boolean existsByEmail(String email);

    // Existing query, keep it if still needed for other dashboards/reports
    @Query("SELECT new com.GestionDepot.GESTION_DEPOT.dto.TopSupplierDto(" +
            "f.nom, " +
            "COUNT(s.id)) " +
            "FROM Fournisseur f LEFT JOIN f.stocks s " +
            "GROUP BY f.nom " +
            "ORDER BY COUNT(s.id) DESC")
    List<TopSupplierDto> findTopSuppliersByProductCount(Pageable pageable);

    // NEW QUERY to get summary data for the main table
    @Query("SELECT new com.GestionDepot.GESTION_DEPOT.dto.FournisseurSummaryDto(" +
            "f.id, f.nom, f.societe, f.email, f.telephone, " +
            "CAST(COALESCE(SUM(s.quantiteProduit), 0) AS int), " + // Explicitly cast to int
            "CAST(COALESCE(SUM(s.prixAchat * s.quantiteProduit), 0.0) AS double)) " + // Explicitly cast to double
            "FROM Fournisseur f LEFT JOIN f.stocks s " +
            "GROUP BY f.id, f.nom, f.societe, f.email, f.telephone")
    List<FournisseurSummaryDto> findAllFournisseurSummaries();

}