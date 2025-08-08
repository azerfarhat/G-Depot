package com.GestionDepot.GESTION_DEPOT.Response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LigneCommandeResponseDto {
    private Long id;
    private Long produitId;
    private String nomProduit;
    private int quantite;
    private BigDecimal prixVenteTotalLigneTTC;
}