package com.GestionDepot.GESTION_DEPOT.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class StockMovementDto {
    private String month;
    private long entries;
    private long exits;
}