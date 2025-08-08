package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.Stock;
import com.GestionDepot.GESTION_DEPOT.dto.AlertDto;
import com.GestionDepot.GESTION_DEPOT.enums.StatutStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Page<Stock> findByStatut(StatutStock statut, Pageable pageable);

    @Query("SELECT s FROM Stock s " +
            "JOIN FETCH s.produit " +
            "JOIN FETCH s.depot " +
            "WHERE s.dateExpiration BETWEEN :startDate AND :endDate " +
            "ORDER BY s.dateExpiration ASC")
    Set<Stock> findStocksExpirantBientot(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Stock s WHERE s.produit.id = :produitId AND s.quantiteProduit > 0 ORDER BY s.dateEntree ASC")
    List<Stock> findDisponibleByProduitFifo(@Param("produitId") Long produitId);

    @Query("SELECT s FROM Stock s WHERE s.produit.id = :produitId AND s.quantiteProduit > 0 ORDER BY s.dateEntree DESC")
    List<Stock> findDisponibleByProduitLifo(@Param("produitId") Long produitId);

    Optional<Stock> findByCodeBarre(String codeBarre);

    List<Stock> findByProduitId(Long produitId);

    @Query("SELECT s.prixVenteTTC FROM Stock s WHERE s.produit.id = :produitId ORDER BY s.dateEntree DESC, s.id DESC LIMIT 1")
    Optional<BigDecimal> findLatestPrixVenteTTCByProduitId(Long produitId);

    boolean existsByFournisseurId(Long fournisseurId);

    boolean existsByProduitId(Long id);


    @Query("SELECT COUNT(p) FROM Produit p " +
            "WHERE (SELECT COALESCE(SUM(s.quantiteProduit), 0) FROM Stock s WHERE s.produit = p) <= p.stockMinimum")
    long countActiveAlerts();

    @Query("SELECT new com.GestionDepot.GESTION_DEPOT.dto.AlertDto(" +
            "p.nom, " +
            "CONCAT('Quantité totale: ', (SELECT COALESCE(SUM(s_msg.quantiteProduit), 0) FROM Stock s_msg WHERE s_msg.produit = p), '. Seuil: ', p.stockMinimum), " +
            "CASE WHEN (SELECT COALESCE(SUM(s_sum.quantiteProduit), 0) FROM Stock s_sum WHERE s_sum.produit = p) <= 0 " +
            "  THEN com.GestionDepot.GESTION_DEPOT.enums.StatutStock.RUPTURE " +
            "  ELSE com.GestionDepot.GESTION_DEPOT.enums.StatutStock.ALERTE END, " +
            "(SELECT MAX(s_date.createdAt) FROM Stock s_date WHERE s_date.produit = p)) " +
            "FROM Produit p " +
            "WHERE (SELECT COALESCE(SUM(s.quantiteProduit), 0) FROM Stock s WHERE s.produit = p) <= p.stockMinimum " +
            "ORDER BY (SELECT COALESCE(SUM(s_order.quantiteProduit), 0) FROM Stock s_order WHERE s_order.produit = p) ASC")
    List<AlertDto> findRecentAlerts();

    // Requête pour les entrées mensuelles (suppose que dateEntree est la référence)
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.dateEntree >= :debutMois AND s.dateEntree <= :finMois")
    long countMonthlyEntries(@Param("debutMois") LocalDate debutMois, @Param("finMois") LocalDate finMois);



    @Query(
            value = "SELECT DATE_FORMAT(s.date_entree, '%Y-%m'), SUM(s.quantite_produit) " +
                    "FROM stocks s " +
                    "WHERE s.date_entree >= :sixMonthsAgo " +
                    "GROUP BY DATE_FORMAT(s.date_entree, '%Y-%m') " +
                    "ORDER BY DATE_FORMAT(s.date_entree, '%Y-%m')",
            nativeQuery = true
    )
    List<Object[]> findMonthlyEntries(@Param("sixMonthsAgo") LocalDate sixMonthsAgo);

    List<Stock> findByProduitIdAndDepotId(Long produitId, Long depotId);

    List<Stock> findByProduitIdAndStatut(Long produitId, StatutStock statut);
}
