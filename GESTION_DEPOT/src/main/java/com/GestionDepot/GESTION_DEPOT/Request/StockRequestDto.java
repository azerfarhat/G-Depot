package com.GestionDepot.GESTION_DEPOT.Request;
import com.GestionDepot.GESTION_DEPOT.Model.Depot;
import com.GestionDepot.GESTION_DEPOT.Model.Fournisseur;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

public class StockRequestDto {
    private Long produitId;
    private Long depotId;
    private Long fournisseurid;
    private String codeBarre;
    private int quantite;
    private int seuilMin;
    private LocalDate dateExpiration;
    private BigDecimal prixVenteHTVA;
    private BigDecimal prixAchat;

    public int getSeuilMin() {
        return seuilMin;
    }

    public void setSeuilMin(int seuilMin) {
        this.seuilMin = seuilMin;
    }

    public Long getFournisseurid() {
        return fournisseurid;
    }

    public void setFournisseurid(Long fournisseurid) {
        this.fournisseurid = fournisseurid;
    }

    public Long getDepotId() {
        return depotId;
    }

    public void setDepotId(Long depotId) {
        this.depotId = depotId;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public Long getProduitId() {
        return produitId;
    }

    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }

    public void setPrixAchat(BigDecimal prixAchat) {
        this.prixAchat = prixAchat;
    }

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }





    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public BigDecimal getPrixVenteHTVA() {
        return prixVenteHTVA;
    }

    public void setPrixVenteHTVA(BigDecimal prixVenteHTVA) {
        this.prixVenteHTVA = prixVenteHTVA;
    }


}
