package com.GestionDepot.GESTION_DEPOT.Controller;

// Assurez-vous d'avoir les bons imports pour les DTOs
import com.GestionDepot.GESTION_DEPOT.dto.*;

import com.GestionDepot.GESTION_DEPOT.Service.BonDeSortieService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/bons-de-sortie")
@CrossOrigin(origins = "*")
public class BonDeSortieController {

    private final BonDeSortieService bonDeSortieService;

    public BonDeSortieController(BonDeSortieService bonDeSortieService) {
        this.bonDeSortieService = bonDeSortieService;
    }

    // Endpoint pour créer un Bon de Sortie
    @PostMapping("/creer/{commandeId}/{chauffeurId}") // <--- MODIFICATION : Ajoutez chauffeurId en param
    public ResponseEntity<BonDeSortieCreeDto> creerBonDeSortie(
            @PathVariable Long commandeId,
            @PathVariable Long chauffeurId) { // <--- MODIFICATION : Prenez chauffeurId
        BonDeSortieCreeDto nouveauBDS = bonDeSortieService.creerBonDeSortie(commandeId, chauffeurId);
        return new ResponseEntity<>(nouveauBDS, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/produits")
    public ResponseEntity<List<ProduitDTO>> getProduitsParBonDeSortie(@PathVariable Long id) {
        try {
            List<ProduitDTO> produits = bonDeSortieService.getProduitsDTOParBonDeSortie(id);
            return ResponseEntity.ok(produits);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    // Endpoint pour récupérer tous les Bons de Sortie
    @GetMapping
    public ResponseEntity<List<BonDeSortieListDTO>> getAllBonsDeSortie() {
        List<BonDeSortieListDTO> allBDS = bonDeSortieService.getAllBonsDeSortie();
        return ResponseEntity.ok(allBDS);
    }

    // Endpoint pour récupérer les détails d'un Bon de Sortie par son ID
    @GetMapping("/{id}/details") // <--- MODIFICATION : Chemin plus clair pour les détails
    public ResponseEntity<?> getBonDeSortieDetails(@PathVariable Long id) {
        try {
            BonDeSortieDetailDto bds = bonDeSortieService.getBonDeSortieDetails(id);
            return ResponseEntity.ok(bds);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Endpoint pour enregistrer les retours
    @PatchMapping("/{id}/retours")
    public ResponseEntity<?> enregistrerRetours(@PathVariable Long id, @Valid @RequestBody List<RetourProduitDTO> retours) {
        try {
            bonDeSortieService.enregistrerRetours(id, retours);
            return ResponseEntity.ok("Retours enregistrés avec succès pour le Bon de Sortie " + id);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Endpoint pour la vérification de cohérence du BDS
    @GetMapping("/{id}/verification")
    public ResponseEntity<?> getVerificationDetails(@PathVariable Long id) {
        try {
            VerificationBDS_DTO verification = bonDeSortieService.getDetailsVerification(id);
            return ResponseEntity.ok(verification);
        } catch (EntityNotFoundException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // --- Gestion centralisée des erreurs ---
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<String> handleNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequestException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}