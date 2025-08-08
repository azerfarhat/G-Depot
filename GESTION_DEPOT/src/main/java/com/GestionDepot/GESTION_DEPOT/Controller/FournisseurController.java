package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Model.Fournisseur;
import com.GestionDepot.GESTION_DEPOT.Service.FournisseurService;
import com.GestionDepot.GESTION_DEPOT.dto.FournisseurSummaryDto; // Import the DTO
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fournisseurs")
public class FournisseurController {

    @Autowired
    private FournisseurService fournisseurService;

    // Existing endpoint, keep it if other parts of your app still need the full Fournisseur object
    @GetMapping
    public List<Fournisseur> getAll() {
        return fournisseurService.getAllFournisseurs();
    }

    // NEW ENDPOINT for the summary data needed for the table
    @GetMapping("/summary")
    public List<FournisseurSummaryDto> getAllFournisseurSummaries() {
        return fournisseurService.getAllFournisseurSummaries();
    }

    //work
    @GetMapping("/get_fournisseur/{id}")
    public Fournisseur getById(@PathVariable Long id) {
        return fournisseurService.getFournisseurById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable"));
    }

    @PostMapping("/ajouter_fournisseurs")
    public ResponseEntity<String> ajouter(@Valid @RequestBody Fournisseur fournisseur) {
        try {
            Fournisseur nouveauFournisseur = fournisseurService.ajouterFournisseur(fournisseur);

            String messageDeSucces = "Le fournisseur '" + nouveauFournisseur.getNom() + "' a été ajouté avec succès.";

            return new ResponseEntity<>(messageDeSucces, HttpStatus.CREATED);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/update_founisseur/{id}")
    public ResponseEntity<Fournisseur> update(@PathVariable Long id, @Valid @RequestBody Fournisseur fournisseur) { // Added @Valid
        try {
            Fournisseur updatedFournisseur = fournisseurService.mettreAJourFournisseur(id, fournisseur);
            return ResponseEntity.ok(updatedFournisseur);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/delete_fournisseur/{id}")
    public ResponseEntity<String> supprimerFournisseur(@PathVariable Long id) {
        try {
            fournisseurService.supprimerFournisseur(id);
            return ResponseEntity.ok("Fournisseur avec l'ID " + id + " supprimé avec succès.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}