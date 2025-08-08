package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Model.*;
import com.GestionDepot.GESTION_DEPOT.Repository.ProduitRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.RetraitStockRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.StockRepository;
import com.GestionDepot.GESTION_DEPOT.Request.StockRequestDto;
import com.GestionDepot.GESTION_DEPOT.Response.StockCreationResponse;
import com.GestionDepot.GESTION_DEPOT.enums.RetreiveProductMethode;
import com.GestionDepot.GESTION_DEPOT.enums.StatutStock;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GestionStockService {

    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final RetraitStockRepository retraitStockRepository;

    @Autowired
    public GestionStockService(ProduitRepository produitRepository,
                               StockRepository stockRepository,
                               RetraitStockRepository retraitStockRepository) {
        this.produitRepository = produitRepository;
        this.stockRepository = stockRepository;
        this.retraitStockRepository = retraitStockRepository;
    }

    public Stock getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Stock non trouvé"));
    }
    public Stock getStockByCodeBarre(String codeBarre) {
        return   stockRepository.findByCodeBarre(codeBarre)
                .orElseThrow(() -> new EntityNotFoundException("Lot de stock non trouvé avec le code-barres : " + codeBarre));
    }
    public Set<Stock> getStocksExpirantBientot(int nbJours) {
        LocalDate debut = LocalDate.now();
        LocalDate fin = debut.plusDays(nbJours);
        return stockRepository.findStocksExpirantBientot(debut, fin);
    }
    @Transactional
    public List<Stock> getAllStocksAvecStatutMisAJour() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            stock.updateStatut();
        }
        stockRepository.saveAll(stocks);
        return stocks;
    }
    public Page<Stock> getStocksParStatut(StatutStock statut, Pageable pageable) {
        return stockRepository.findByStatut(statut, pageable);
    }
    public StockCreationResponse ajouterNouveauStock(
            StockRequestDto dto,
            Produit produit,
            Depot depot,
            Fournisseur fournisseur
    ) {
        if (dto.getQuantite() <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        Stock stock = new Stock();
        stock.setProduit(produit);
        stock.setDepot(depot);
        stock.setFournisseur(fournisseur);
        stock.setCodeBarre(dto.getCodeBarre());
        stock.setSeuilMin(dto.getSeuilMin());
        stock.setQuantiteProduit(dto.getQuantite());
        stock.setDateExpiration(dto.getDateExpiration());
        stock.setPrixVenteHTVA(dto.getPrixVenteHTVA());
        stock.setPrixAchat(dto.getPrixAchat());
        stock.setDateEntree(LocalDate.now());
        stock.calculerEtSetPrixTTC();
        stock.updateStatut();
        Stock saved = stockRepository.save(stock);
        return new StockCreationResponse(false, "Stock ajouté avec succès", saved);
    }
    @Transactional
    public void supprimerStock(Long stockId) {
        Stock stockASupprimer = stockRepository.findById(stockId)
                .orElseThrow(() -> new NoSuchElementException("Le stock avec l'ID " + stockId + " n'existe pas."));
        if (stockASupprimer.getQuantiteProduit() > 0) {
            throw new IllegalStateException("Impossible de supprimer un stock qui contient encore des produits.");
        }
        stockRepository.delete(stockASupprimer);
    }
    @Transactional
    public List<RetraitStock> deduireStockAvecRetrait(Produit produit, int quantiteDemandee, LigneCommande ligneCommande) {
        if (quantiteDemandee <= 0) {
            throw new IllegalArgumentException("La quantité demandée doit être positive.");
        }
        RetreiveProductMethode methode = produit.getStrategieStock() != null ? produit.getStrategieStock() : RetreiveProductMethode.FIFO;
        List<Stock> stocksDisponibles;
        if (methode == RetreiveProductMethode.LIFO) {
            stocksDisponibles = stockRepository.findDisponibleByProduitLifo(produit.getId());
        } else {
            stocksDisponibles = stockRepository.findDisponibleByProduitFifo(produit.getId());
        }
        int stockTotal = stocksDisponibles.stream().mapToInt(Stock::getQuantiteProduit).sum();
        if (stockTotal < quantiteDemandee) {
            throw new IllegalStateException("Stock insuffisant pour le produit " + produit.getNom() + ". Demandé : " + quantiteDemandee + ", Disponible : " + stockTotal);
        }
        int quantiteRestante = quantiteDemandee;
        List<RetraitStock> retraits = new ArrayList<>();
        List<Stock> stocksAMettreAJour = new ArrayList<>();
        for (Stock stock : stocksDisponibles) {
            if (quantiteRestante <= 0) break;
            int quantiteARetirer = Math.min(stock.getQuantiteProduit(), quantiteRestante);
            RetraitStock retrait = new RetraitStock(stock, ligneCommande, quantiteARetirer, LocalDateTime.now());
            retraits.add(retrait);
            stock.setQuantiteProduit(stock.getQuantiteProduit() - quantiteARetirer);
            stock.updateStatut();
            stocksAMettreAJour.add(stock);
            quantiteRestante -= quantiteARetirer;
        }
        stockRepository.saveAll(stocksAMettreAJour);
        return retraits;
    }
    public List<Stock> findByProduitId(Long produitId) {
        return stockRepository.findByProduitId(produitId);
    }

    public BigDecimal getDernierPrixVenteTTC(Long produitId) {
        return stockRepository.findByProduitIdAndStatut(produitId, StatutStock.DISPONIBLE)
                .stream()
                .max(Comparator.comparing(Stock::getDateEntree))
                .map(Stock::getPrixVenteTTC)
                .orElseThrow(() -> new IllegalStateException("Aucun stock disponible ou prix de vente trouvé pour le produit " + produitId));
    }

    @Transactional
    public void decrementerStock(Produit produit, int quantiteADeduire) {
        if (quantiteADeduire <= 0) {
            throw new IllegalArgumentException("La quantité à déduire doit être positive.");
        }
        RetreiveProductMethode methode = produit.getStrategieStock() != null ? produit.getStrategieStock() : RetreiveProductMethode.FIFO;
        List<Stock> stocksDisponibles = (methode == RetreiveProductMethode.LIFO)
                ? stockRepository.findDisponibleByProduitLifo(produit.getId())
                : stockRepository.findDisponibleByProduitFifo(produit.getId());
        int stockTotal = stocksDisponibles.stream().mapToInt(Stock::getQuantiteProduit).sum();
        if (stockTotal < quantiteADeduire) {
            throw new IllegalStateException("Stock insuffisant pour le produit " + produit.getNom() + ". Demandé : " + quantiteADeduire + ", Disponible : " + stockTotal);
        }
        int quantiteRestante = quantiteADeduire;
        List<Stock> stocksAMettreAJour = new ArrayList<>();
        for (Stock stock : stocksDisponibles) {
            if (quantiteRestante <= 0) break;
            int quantiteARetirer = Math.min(stock.getQuantiteProduit(), quantiteRestante);
            stock.setQuantiteProduit(stock.getQuantiteProduit() - quantiteARetirer);
            stocksAMettreAJour.add(stock);
            quantiteRestante -= quantiteARetirer;
        }
        stockRepository.saveAll(stocksAMettreAJour);
    }

    @Transactional
    public void reintegrerStock(Long produitId, int quantiteAReintegrer) {
        if (quantiteAReintegrer <= 0) return;
        Stock lotLePlusRecent = stockRepository.findByProduitId(produitId)
                .stream()
                .max(Comparator.comparing(Stock::getDateEntree))
                .orElseThrow(() -> new IllegalStateException("Impossible de retourner le produit " + produitId + ", aucun lot de stock n'existe."));
        lotLePlusRecent.setQuantiteProduit(lotLePlusRecent.getQuantiteProduit() + quantiteAReintegrer);
        stockRepository.save(lotLePlusRecent);
    }
}