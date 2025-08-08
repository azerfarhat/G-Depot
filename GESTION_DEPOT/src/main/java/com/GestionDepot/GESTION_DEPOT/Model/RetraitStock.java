package com.GestionDepot.GESTION_DEPOT.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // NOUVEL IMPORT

import java.time.LocalDateTime;

@Entity
@Table(name = "retrait_stock")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id") // <--- CETTE LIGNE EST CRUCIALE !
public class RetraitStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    @JsonBackReference("stock-retraits")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_commande_id", nullable = false)
    @JsonBackReference("ligneCommande-retraits")
    private LigneCommande ligneCommande;

    @Column(nullable = false)
    private int quantiteRetiree;

    @Column(nullable = false)
    private LocalDateTime dateRetrait;

    public RetraitStock(Stock stock, LigneCommande ligneCommande, int quantiteRetiree, LocalDateTime dateRetrait) {
        this.stock = stock;
        this.ligneCommande = ligneCommande;
        this.quantiteRetiree = quantiteRetiree;
        this.dateRetrait = dateRetrait;
    }
}