package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Request.ProduitUpdateDTO;
import com.GestionDepot.GESTION_DEPOT.Model.Produit;
import com.GestionDepot.GESTION_DEPOT.Repository.ProduitRepository;
import com.GestionDepot.GESTION_DEPOT.Service.GestionProduitService;
import com.GestionDepot.GESTION_DEPOT.dto.ProduitListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*") // On autorise tout pour le test
public class ProduitController {

    @Autowired
    private GestionProduitService produitService;

    @Autowired
    private ProduitRepository produitRepository;

    @PostMapping("/ajouter_produit")
    public ResponseEntity<?> ajouterProduit(@Valid @RequestBody Produit produit) {
        try {
            System.out.println("Tentative d'ajout du produit: " + produit);
            Produit nouveauProduit = produitService.creerProduit(produit);
            return ResponseEntity.ok(nouveauProduit);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du produit: " + e.getMessage());
            e.printStackTrace(); // Ajout pour afficher la pile d'exécution complète

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'ajout du produit : " + e.getMessage());
        }
    }

    @GetMapping("/liste")
    public ResponseEntity<List<ProduitListDTO>> getProductList(
            @RequestParam(required = false) String recherche) { // On accepte un paramètre optionnel

        List<ProduitListDTO> produitsListe = produitService.rechercherProduits(recherche);
        return ResponseEntity.ok(produitsListe);
    }
    @GetMapping("/get_product/{id}")
    public ResponseEntity<Produit> getProduit(@PathVariable Long id) {
        return produitService.trouverProduitParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update_product/{id}")
    public ResponseEntity<Produit> updateProduit(@PathVariable Long id, @Validated @RequestBody ProduitUpdateDTO dto) {
        Produit updatedProduit = produitService.mettreAJourProduit(id, dto);
        return ResponseEntity.ok(updatedProduit);
    }

    @DeleteMapping("/delete_product/{id}")
    public ResponseEntity<String> deleteProduit(@PathVariable Long id) {
        try {
            boolean aEteSupprime = produitService.supprimerProduit(id);
            if (aEteSupprime) {
                return ResponseEntity.ok("Produit avec l'ID " + id + " supprimé avec succès.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Le produit avec l'ID " + id + " n'existe pas.");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Impossible de supprimer : le produit est utilisé ailleurs (par exemple, dans une commande).");
        }
    }

}
