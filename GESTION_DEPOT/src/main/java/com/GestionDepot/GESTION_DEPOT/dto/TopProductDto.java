package com.GestionDepot.GESTION_DEPOT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopProductDto {
    private String name;
    private Long movements;

    public TopProductDto(String name, Long movements, Long totalQuantity) {
        this.name = name;
        this.movements = movements;
    }
}