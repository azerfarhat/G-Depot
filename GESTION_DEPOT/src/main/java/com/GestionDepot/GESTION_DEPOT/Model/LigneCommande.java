package com.GestionDepot.GESTION_DEPOT.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "lignes_commande")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"commande", "produit", "retraits"})
@EqualsAndHashCode(of = "id")
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantite;

    @Column(name = "prix_unitaire_ttc")
    private BigDecimal prixUnitaireTTC;

    @Column(name = "prix_vente_total_ligne_ttc")
    private BigDecimal prixVenteTotalLigneTTC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    @JsonBackReference("commande-lignes") // *** Doit correspondre EXACTEMENT au nom de @JsonManagedReference dans Commande ***
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id")
    @JsonBackReference("produit-lignesCommande") // Si Produit a une liste de LigneCommande
    private Produit produit;

    @OneToMany(mappedBy = "ligneCommande", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("ligneCommande-retraits") // Nom unique pour la référence managée
    private Set<RetraitStock> retraits = new HashSet<>();

    public void setRetraits(List<RetraitStock> nouveauxRetraits) {
        if (this.retraits != null) {
            for (RetraitStock ancienRetrait : this.retraits) {
                ancienRetrait.setLigneCommande(null);
            }
        }
        this.retraits.clear();
        if (nouveauxRetraits != null) {
            for (RetraitStock nouveauRetrait : nouveauxRetraits) {
                this.addRetrait(nouveauRetrait);
            }
        }
    }

    public void calculerPrixTotalLigne() {
        if (this.prixUnitaireTTC == null) {
            throw new IllegalStateException("Impossible de calculer le total de la ligne : le prix unitaire n'est pas défini.");
        }

        // Le calcul est maintenant une simple multiplication.
        BigDecimal quantiteBD = new BigDecimal(this.quantite);
        this.prixVenteTotalLigneTTC = this.prixUnitaireTTC.multiply(quantiteBD);
    }

    public void addRetrait(RetraitStock retrait) {
        retraits.add(retrait);
        retrait.setLigneCommande(this);
    }

}