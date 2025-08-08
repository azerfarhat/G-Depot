package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.RetraitStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RetraitStockRepository extends JpaRepository<RetraitStock, Long> {
 @Query("SELECT COUNT(rs) FROM RetraitStock rs WHERE rs.dateRetrait >= :debutMois AND rs.dateRetrait <= :finMois")
 long countMonthlyExits(@Param("debutMois") LocalDateTime debutMois, @Param("finMois") LocalDateTime finMois);

 @Query(
         value = "SELECT DATE_FORMAT(rs.date_retrait, '%Y-%m'), SUM(rs.quantite_retiree) " +
                 "FROM retrait_stock rs " +
                 "WHERE rs.date_retrait >= :sixMonthsAgo " +
                 "GROUP BY DATE_FORMAT(rs.date_retrait, '%Y-%m') " +
                 "ORDER BY DATE_FORMAT(rs.date_retrait, '%Y-%m')",
         nativeQuery = true
 )
 List<Object[]> findMonthlyExits(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);
}
