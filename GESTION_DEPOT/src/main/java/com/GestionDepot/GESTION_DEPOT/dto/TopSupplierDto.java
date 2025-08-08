package com.GestionDepot.GESTION_DEPOT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSupplierDto {
    private String supplierName;
    private Long productsCount;

}