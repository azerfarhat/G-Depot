package com.GestionDepot.GESTION_DEPOT.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data @AllArgsConstructor
public class DashboardStatsDto {
    private long totalProducts;
    private long monthlyEntries;
    private long monthlyExits;
    private long activeAlerts;
    private BigDecimal monthlyRevenue;
    private long totalOrders;
    private long totalUsers;
    private Double changeInEntries;
    private Double changeInExits;
    private Double changeInRevenue;

    public DashboardStatsDto(long totalProducts, long monthlyEntries, long monthlyExits, long activeAlerts, BigDecimal monthlyRevenue, long totalOrders, long totalUsers) {
        this.totalProducts = totalProducts;
        this.monthlyEntries = monthlyEntries;
        this.monthlyExits = monthlyExits;
        this.activeAlerts = activeAlerts;
        this.monthlyRevenue = monthlyRevenue;
        this.totalOrders = totalOrders;
        this.totalUsers = totalUsers;
    }
}