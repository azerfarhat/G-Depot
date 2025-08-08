package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Model.*;
import com.GestionDepot.GESTION_DEPOT.Repository.DepotRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.FournisseurRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.ProduitRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.StockRepository;
import com.GestionDepot.GESTION_DEPOT.Request.StockRequestDto;
import com.GestionDepot.GESTION_DEPOT.enums.StatutStock;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import com.GestionDepot.GESTION_DEPOT.Service.GestionStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping(value = "/stock")
public class StockController {

    @Autowired
    private GestionStockService gestionStockService;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    DepotRepository depotRepository;

    @Autowired
    FournisseurRepository fournisseurRepository;
    @Autowired
    private StockRepository stockRepository;

    @GetMapping("/get_by_id/{stockId}")
    public ResponseEntity<?> getStockById(@PathVariable Long stockId) {
        try {
            Stock stock = gestionStockService.getStockById(stockId);
            return ResponseEntity.ok(stock);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun stock trouvé avec l'ID " + stockId);
        }
    }
        @GetMapping("/produit/{produitId}")
        public ResponseEntity<List<Stock>> getStocksByProduitId(@PathVariable Long produitId) {
            List<Stock> stocks = gestionStockService.findByProduitId(produitId);
            return ResponseEntity.ok(stocks);
        }

    @GetMapping("/liste_stock")
    public ResponseEntity<List<Stock>> getAllStocks() {
        List<Stock> stocks = gestionStockService.getAllStocksAvecStatutMisAJour();

        if (stocks.isEmpty()) {
            System.out.println("erreurddd");
        }

        return new ResponseEntity<>(stocks, HttpStatus.OK);
    }

    //je veux voir en link expirant dans ? 15 jour exemple
    @GetMapping("/admin_view_expirant")
    public ResponseEntity<?> getStocksExpirantEntreDate(@RequestParam(name = "dans") int nbJours) {
        Set<Stock> stocks = gestionStockService.getStocksExpirantBientot(nbJours);

        if (stocks.isEmpty()) {
            return new ResponseEntity<>("Aucun stock n'expire dans " + nbJours + " jours.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(stocks, HttpStatus.OK);
    }

    @GetMapping("/admin_view/par-statut/{statut}")
    public Page<Stock> getStocksParStatut(@PathVariable StatutStock statut, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return gestionStockService.getStocksParStatut(statut, pageable);
    }

    @PostMapping("/ajouter_stock")
    public ResponseEntity<?> ajouterStock(@RequestBody @Valid StockRequestDto dto) {
        try {
            Produit produit = produitRepository.findById(dto.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));

            Depot depot = depotRepository.findById(dto.getDepotId())
                    .orElseThrow(() -> new EntityNotFoundException("Dépôt introuvable"));

            Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurid())
                    .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

            Stock nouveauStock = gestionStockService.ajouterNouveauStock(dto, produit, depot, fournisseur).getStock();

            return new ResponseEntity<>(nouveauStock, HttpStatus.CREATED);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/admin_view/stocks/{id}")
    public ResponseEntity<?> supprimerStock(@PathVariable Long id) {
        try {
            gestionStockService.supprimerStock(id);
            return ResponseEntity.ok("Stock supprimé avec succès.");
        } catch (NoSuchElementException e) {
            // Stock non trouvé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            // Tentative de suppression d’un stock avec quantité > 0
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Erreur non prévue
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne lors de la suppression.");
        }
    }

    @GetMapping("/admin_view/get_stock_bycodebarre")
    public ResponseEntity<?> getstockbycodeabarre(@RequestBody String codeabarre) {
        try {
            Stock stock = gestionStockService.getStockByCodeBarre(codeabarre);
            return ResponseEntity.ok(stock);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun stock trouvé avec l'ID " + codeabarre);
        }
    }
}
