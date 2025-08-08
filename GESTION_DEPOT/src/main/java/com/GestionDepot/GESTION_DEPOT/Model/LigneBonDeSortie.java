package com.GestionDepot.GESTION_DEPOT.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // NOUVEL IMPORT

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_bon_de_sortie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id") // TRÈS IMPORTANT pour les Set de lignes
public class LigneBonDeSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantiteSortie;      // Quantité initiale de ce produit dans le bon de sortie
    private int quantiteRetournee = 0; // Quantité de ce produit qui a été retournée (non livrée / non vendue)

    private int quantiteFacturee = 0; // <--- CHAMP CLÉ : Quantité de ce produit déjà facturée

    @Column(name = "prix_unitaire_ttc", precision = 10, scale = 2)
    private BigDecimal prixUnitaireTTC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id")
    @JsonIgnore
    private BonDeSortie bonDeSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    // Méthode utilitaire pour obtenir la quantité restante disponible à la facturation
    @Transient
    public int getQuantiteDisponiblePourFacturation() {
        return quantiteSortie - quantiteRetournee - quantiteFacturee;
    }
}