package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Model.Depot;
import com.GestionDepot.GESTION_DEPOT.Service.DepotService;
import com.GestionDepot.GESTION_DEPOT.Dto.DepotDto; // Ensure this is imported
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/depot")
public class DepotController {

    @Autowired
    private DepotService depotService;

    @PostMapping("/ajouter_Depot/responsable/{responsableId}")
    public ResponseEntity<?> ajouterDepot(
            @RequestBody Depot depot,
            @PathVariable Long responsableId) {

        if (depot.getNom() == null || depot.getNom().isEmpty()) {
            return ResponseEntity.badRequest().body("Le nom du dépôt est requis.");
        }

        try {
            Depot nouveauDepot = depotService.ajouterDepot(depot, responsableId);
            // MODIFIÉ : Retourne maintenant un DTO au lieu de l'entité brute
            return ResponseEntity.ok(depotService.getDepotDtoById(nouveauDepot.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepot(@PathVariable Long id, @RequestBody Depot depot) {
        if (depot.getNom() == null || depot.getNom().isEmpty()) {
            return ResponseEntity.badRequest().body("Le nom du dépôt est requis pour la mise à jour.");
        }
        try {
            Depot updatedDepotEntity = depotService.updateDepot(id, depot); // Get the updated entity
            // MODIFIÉ : Retourne maintenant un DTO au lieu de l'entité brute
            return ResponseEntity.ok(depotService.getDepotDtoById(updatedDepotEntity.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<DepotDto>> getAllDepots() {
        List<DepotDto> depots = depotService.getAllDepotsWithCalculatedCa();
        return ResponseEntity.ok(depots);
    }

    @GetMapping("/responsable/{idResponsable}")
    public ResponseEntity<?> getDepotsParResponsable(@PathVariable Long idResponsable) {
        try {
            List<DepotDto> depots = depotService.getDepotsParResponsableAsDto(idResponsable);
            return ResponseEntity.ok(depots);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete_Depot/{id}")
    public ResponseEntity<?> supprimerDepot(@PathVariable Long id) {
        try {
            depotService.supprimerDepot(id);
            return ResponseEntity.ok("Dépôt supprimé avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get_depot/{id}")
    public ResponseEntity<?> getDepotById(@PathVariable Long id) {
        try {
            DepotDto depot = depotService.getDepotDtoById(id);
            return ResponseEntity.ok(depot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tva/{id}")
    public ResponseEntity<String> modifierTvaDepot(@PathVariable Long id, @RequestBody BigDecimal nouvelleTva) {
        try {
            depotService.modifierTvaDepot(id, nouvelleTva);
            return ResponseEntity.ok("TVA modifiée avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{depotId}/assigner-responsable/{utilisateurId}")
    public ResponseEntity<String> assignerResponsable(
            @PathVariable Long depotId,
            @PathVariable Long utilisateurId
    ) {
        try {
            depotService.assignerResponsableAuDepot(utilisateurId, depotId);
            return ResponseEntity.ok("Responsable assigné avec succès au dépôt.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/details")
    public ResponseEntity<List<DepotDto>> getAllDepotsForDisplay() {
        List<DepotDto> depots = depotService.getAllDepotsWithCalculatedCa();
        return ResponseEntity.ok(depots);
    }
}