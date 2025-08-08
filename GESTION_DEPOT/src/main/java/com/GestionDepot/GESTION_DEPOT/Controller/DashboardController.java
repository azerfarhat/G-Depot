package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Service.DashboardService;
import com.GestionDepot.GESTION_DEPOT.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard") // Le chemin de base pour tous les endpoints du dashboard
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permet les appels depuis votre Angular (http://localhost:4200)
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/stock-movements")
    public ResponseEntity<List<StockMovementDto>> getStockMovements() {
        return ResponseEntity.ok(dashboardService.getStockMovements());
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDto>> getTopProducts() {
        return ResponseEntity.ok(dashboardService.getTopProducts());
    }

    @GetMapping("/top-suppliers")
    public ResponseEntity<List<TopSupplierDto>> getTopSuppliers() {
        return ResponseEntity.ok(dashboardService.getTopSuppliersByProductCount()); // <-- Appelle la méthode correcte
    }

    @GetMapping("/alerts/recent")
    public ResponseEntity<List<AlertDto>> getRecentAlerts() {
        return ResponseEntity.ok(dashboardService.getRecentAlerts());
    }


}