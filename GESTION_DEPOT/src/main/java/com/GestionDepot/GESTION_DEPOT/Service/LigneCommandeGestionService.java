package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Model.*;
import com.GestionDepot.GESTION_DEPOT.Repository.*;
import com.GestionDepot.GESTION_DEPOT.Request.LigneCommandeRequestDto;
import com.GestionDepot.GESTION_DEPOT.Response.CommandeResponseDto;
import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LigneCommandeGestionService {

    private final LigneCommandeRepository ligneCommandeRepository;
    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;

    private final StockRepository stockRepository;
    private final RetraitStockRepository retraitStockRepository;
    private final CommandeService commandeService;
    private final GestionStockService gestionStockService;

    @Autowired
    public LigneCommandeGestionService(LigneCommandeRepository ligneCommandeRepository, CommandeRepository commandeRepository, ProduitRepository produitRepository, StockRepository stockRepository, RetraitStockRepository retraitStockRepository, @Lazy CommandeService commandeService, GestionStockService gestionStockService) {
        this.ligneCommandeRepository = ligneCommandeRepository;
        this.commandeRepository = commandeRepository;
        this.produitRepository = produitRepository;
        this.stockRepository = stockRepository;
        this.retraitStockRepository = retraitStockRepository;

        this.commandeService = commandeService;
        this.gestionStockService = gestionStockService;
    }

    @Transactional
    public CommandeResponseDto ajouterLigneACommande(Long commandeId, LigneCommandeRequestDto dto) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée : " + commandeId));
        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + dto.getProduitId()));

        if (commande.getStatutCommande() != StatutCommande.EN_COURS) {
            throw new IllegalStateException("Impossible d'ajouter une ligne : la commande n'est plus en cours. Statut actuel : " + commande.getStatutCommande());
        }

        BigDecimal prixDeVenteActuel = stockRepository.findLatestPrixVenteTTCByProduitId(produit.getId())
                .orElseThrow(() -> new IllegalStateException("Impossible de déterminer le prix pour le produit '" + produit.getNom() + "'. Aucun stock trouvé."));

        LigneCommande nouvelleLigne = new LigneCommande();
        nouvelleLigne.setProduit(produit);
        nouvelleLigne.setQuantite(dto.getQuantite());
        nouvelleLigne.setPrixUnitaireTTC(prixDeVenteActuel);

        BigDecimal totalLigne = prixDeVenteActuel.multiply(BigDecimal.valueOf(dto.getQuantite()));
        nouvelleLigne.setPrixVenteTotalLigneTTC(totalLigne);

        List<RetraitStock> retraits = gestionStockService.deduireStockAvecRetrait(produit, dto.getQuantite(), nouvelleLigne);
        nouvelleLigne.setRetraits(retraits);

        commande.addLigne(nouvelleLigne);
        ligneCommandeRepository.save(nouvelleLigne);

        commandeService.mettreAJourTotalCommande(commande);

        return commandeService.mapCommandeToDto(commande);
    }

    @Transactional
    public CommandeResponseDto supprimerLigneDeCommande(Long commandeId, Long ligneId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée : " + commandeId));
        LigneCommande ligne = ligneCommandeRepository.findById(ligneId)
                .orElseThrow(() -> new NoSuchElementException("Ligne de commande introuvable : " + ligneId));

        if (commande.getStatutCommande() != StatutCommande.EN_COURS) {
            throw new IllegalStateException("Impossible de supprimer une ligne : la commande n'est plus en cours. Statut actuel : " + commande.getStatutCommande());
        }

        if (!commande.getLignes().contains(ligne)) {
            throw new IllegalStateException("La ligne de commande n'appartient pas à cette commande.");
        }

        // Restituer le stock
        for (RetraitStock retrait : new ArrayList<>(ligne.getRetraits())) {
            Stock stockARestituer = retrait.getStock();
            int quantiteARestituer = retrait.getQuantiteRetiree();
            if (stockARestituer != null) {
                stockARestituer.setQuantiteProduit(stockARestituer.getQuantiteProduit() + quantiteARestituer);
                stockARestituer.updateStatut();
                stockRepository.save(stockARestituer);
            }
        }

        commande.getLignes().remove(ligne);
        ligneCommandeRepository.delete(ligne);
        commandeService.mettreAJourTotalCommande(commande);

        return commandeService.mapCommandeToDto(commande);
    }

    @Transactional
    public CommandeResponseDto mettreAJourQuantiteLigne(Long commandeId, Long ligneId, int nouvelleQuantite) {Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée : " + commandeId));
        LigneCommande ligne = ligneCommandeRepository.findById(ligneId)
                .orElseThrow(() -> new NoSuchElementException("Ligne de commande introuvable : " + ligneId));

        if (commande.getStatutCommande() != StatutCommande.EN_COURS) {
            throw new IllegalStateException("Impossible de modifier une ligne : la commande n'est plus en cours.");
        }

        if (nouvelleQuantite <= 0) {
            return supprimerLigneDeCommande(commandeId, ligneId);
        }

        int ancienneQuantite = ligne.getQuantite();
        int difference = nouvelleQuantite - ancienneQuantite;

        if (difference == 0) {
            return commandeService.mapCommandeToDto(commande);
        }

        if (difference > 0) {

            BigDecimal prixDeVenteActuel = stockRepository.findLatestPrixVenteTTCByProduitId(ligne.getProduit().getId())
                    .orElseThrow(() -> new IllegalStateException("Impossible de déterminer le prix pour le produit '" + ligne.getProduit().getNom() + "'."));

            ligne.setPrixUnitaireTTC(prixDeVenteActuel);

            List<RetraitStock> nouveauxRetraits = gestionStockService.deduireStockAvecRetrait(ligne.getProduit(), difference, ligne);
            nouveauxRetraits.forEach(ligne::addRetrait);

        } else { // difference < 0
            // La logique de restitution de stock est correcte et reste inchangée.
            int quantiteARestituer = -difference;
            List<RetraitStock> retraitsTries = new ArrayList<>(ligne.getRetraits());
            retraitsTries.sort((r1, r2) -> r2.getId().compareTo(r1.getId()));

            Iterator<RetraitStock> iterator = retraitsTries.iterator();
            while (iterator.hasNext() && quantiteARestituer > 0) {
                RetraitStock retrait = iterator.next();
                int quantiteRestituable = Math.min(retrait.getQuantiteRetiree(), quantiteARestituer);
                Stock stockARestituer = retrait.getStock();
                stockARestituer.setQuantiteProduit(stockARestituer.getQuantiteProduit() + quantiteRestituable);
                stockARestituer.updateStatut();
                stockRepository.save(stockARestituer);

                retrait.setQuantiteRetiree(retrait.getQuantiteRetiree() - quantiteRestituable);
                quantiteARestituer -= quantiteRestituable;

                if (retrait.getQuantiteRetiree() == 0) {
                    iterator.remove(); // sécurise la boucle
                    ligne.getRetraits().remove(retrait); // retire de la relation Java (important pour Hibernate)
                    retrait.setLigneCommande(null); // facultatif si bidirectionnel
                    retraitStockRepository.delete(retrait); // maintenant tu peux supprimer
                }

            }
        }
        ligne.setQuantite(nouvelleQuantite);

        ligne.calculerPrixTotalLigne();

        ligneCommandeRepository.save(ligne);

        // On met à jour le total global de la commande.
        commandeService.mettreAJourTotalCommande(commande);

        return commandeService.mapCommandeToDto(commande);
    }

}