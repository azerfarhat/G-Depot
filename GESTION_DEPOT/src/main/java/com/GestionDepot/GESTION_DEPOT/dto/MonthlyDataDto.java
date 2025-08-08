package com.GestionDepot.GESTION_DEPOT.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MonthlyDataDto {

    private String month;

    private Long total;
    public MonthlyDataDto(String month, Long total) {
        this.month = month;
        this.total = total;
    }
}