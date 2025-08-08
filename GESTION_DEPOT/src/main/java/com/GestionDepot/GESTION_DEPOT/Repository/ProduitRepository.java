package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.Produit;
import com.GestionDepot.GESTION_DEPOT.dto.ProduitListDTO;
import com.GestionDepot.GESTION_DEPOT.dto.TopProductDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Query("SELECT new com.GestionDepot.GESTION_DEPOT.dto.TopProductDto(" +
            "p.nom, " +                                          // 1. name (String)
            "COUNT(s.id), " +                                    // 2. movements (Long)
            "(SELECT COALESCE(SUM(sq.quantiteProduit), 0) FROM Stock sq WHERE sq.produit = p)" + // 3. totalQuantity (Long)
            ") " +
            "FROM Stock s JOIN s.produit p " +
            "GROUP BY p.id, p.nom " + // On ne groupe plus par stockMinimum car il n'est plus utilisé ici
            "ORDER BY COUNT(s.id) DESC")
    List<TopProductDto> findTopActiveProducts(Pageable pageable);

    @Query("SELECT new com.GestionDepot.GESTION_DEPOT.dto.ProduitListDTO(" +
            "p.id, " +
            "p.nom, " +
            "p.categorie, " +
            "COALESCE(SUM(s.quantiteProduit), 0L), " +
            "p.stockMinimum, " +
            "COUNT(s.id), " +
            // Sous-requête pour le dernier prix HTVA
            "(SELECT s2.prixVenteHTVA FROM Stock s2 WHERE s2.produit.id = p.id ORDER BY s2.createdAt DESC LIMIT 1), " +
            // Sous-requête pour le dernier prix TTC
            "(SELECT s3.prixVenteTTC FROM Stock s3 WHERE s3.produit.id = p.id ORDER BY s3.createdAt DESC LIMIT 1), " +

             "p.description, " +      // 1. On sélectionne la description
            "p.strategieStock" +     // 2. On sélectionne la stratégie de stock

            ") " +
            "FROM Produit p " +
            "LEFT JOIN p.stocks s " +
            "GROUP BY p.id, p.nom, p.categorie, p.stockMinimum, p.description, p.strategieStock") // 3. On ajoute les champs au GROUP BY
    List<ProduitListDTO> findProduitListDTOs();

    @Query("SELECT new com.GestionDepot.GESTION_DEPOT.dto.ProduitListDTO(" +
            "p.id, " +
            "p.nom, " +
            "p.categorie, " +
            "COALESCE(SUM(s.quantiteProduit), 0L), " +
            "p.stockMinimum, " +
            "COUNT(s.id), " +
            // Sous-requête pour le dernier prix HTVA
            "(SELECT s2.prixVenteHTVA FROM Stock s2 WHERE s2.produit.id = p.id ORDER BY s2.createdAt DESC LIMIT 1), " +
            // Sous-requête pour le dernier prix TTC
            "(SELECT s3.prixVenteTTC FROM Stock s3 WHERE s3.produit.id = p.id ORDER BY s3.createdAt DESC LIMIT 1), " +
            "p.description, " +
            "p.strategieStock" +
            ") " +
            "FROM Produit p " +
            "LEFT JOIN p.stocks s " +
            "WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "LOWER(p.categorie) LIKE LOWER(CONCAT('%', :terme, '%')) OR " +
            "CAST(p.id as string) LIKE CONCAT('%', :terme, '%') " +
            "GROUP BY p.id, p.nom, p.categorie, p.stockMinimum, p.description, p.strategieStock")
    List<ProduitListDTO> searchProduitListDTOs(@Param("terme") String terme);



}

