package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Model.*;
import com.GestionDepot.GESTION_DEPOT.Repository.*;
import com.GestionDepot.GESTION_DEPOT.dto.*; // Vos DTOs BonDeSortieCreeDto, BonDeSortieListDTO, BonDeSortieDetailDto, LigneBdsDetailDto, FactureSimpleDto, RetourProduitDTO
import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import com.GestionDepot.GESTION_DEPOT.enums.StatutBonDeSortie;
import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture; // Ajout pour FactureSimpleDto
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode; // Pour les calculs de prix
import java.time.LocalDate;
import java.util.ArrayList; // Pour les listes mutables
import java.util.List;
import java.util.Set;
import java.util.UUID; // Pour numéro de BDS aléatoire
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok génère le constructeur avec tous les final fields
public class BonDeSortieService {

    private final BonDeSortieRepository bonDeSortieRepository;
    private final CommandeRepository commandeRepository;
    private final LigneBonDeSortieRepository ligneBonDeSortieRepository;
    private final GestionStockService gestionStockService;
    private final UtilisateurRepository utilisateurRepository; // <--- NOUVELLE DÉPENDANCE : Pour chercher le chauffeur
    private final ProduitRepository produitRepository; // <--- NOUVELLE DÉPENDANCE : Pour chercher le produit pour les lignes

    // Le constructeur est généré par @RequiredArgsConstructor avec Lombok

    @Transactional
    public BonDeSortieCreeDto creerBonDeSortie(Long commandeId, Long chauffeurId) { // <--- MODIFICATION : Ajoutez chauffeurId en param
        // 1. Récupérer la commande et valider son état.
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'id : " + commandeId));

        if (commande.getStatutCommande() != StatutCommande.VALIDEE) {
            throw new IllegalStateException("Un Bon de Sortie ne peut être créé qu'à partir d'une commande au statut VALIDEE.");
        }
        if (commande.getLignes() == null || commande.getLignes().isEmpty()) {
            throw new IllegalStateException("La commande est vide et ne peut pas générer de bon de sortie.");
        }
        if (bonDeSortieRepository.existsByCommandeOrigineId(commandeId)) {
            throw new IllegalStateException("Un Bon de Sortie a déjà été généré pour cette commande.");
        }

        // Récupérer le chauffeur
        Utilisateur chauffeur = utilisateurRepository.findById(chauffeurId)
                .filter(u -> u.getRole() == RoleUtilisateur.CHAUFFEUR)
                .orElseThrow(() -> new EntityNotFoundException("Chauffeur non trouvé avec l'id : " + chauffeurId));


        // 2. Créer l'en-tête du Bon de Sortie.
        BonDeSortie bds = new BonDeSortie();
        bds.setNumeroBDS("BDS-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase()); // Format plus propre
        bds.setDateSortie(LocalDate.now());
        bds.setStatut(StatutBonDeSortie.CREE); // Le statut initial devrait être CRÉE ou EN_COURS, pas LIVRE.
        bds.setChauffeur(chauffeur); // <--- ASSIGNATION CORRECTE DU CHAUFFEUR
        bds.setCommandeOrigine(commande);

        BigDecimal valeurTotaleInitialeTTC = BigDecimal.ZERO;
        List<LigneBonDeSortie> lignesBdsACreer = new ArrayList<>(); // Collecter les lignes à sauvegarder

        // 3. Créer les lignes du BDS et décrémenter le stock.
        for (LigneCommande ligneCommande : commande.getLignes()) {
            if (ligneCommande.getProduit() == null) {
                throw new IllegalStateException("Une ligne de commande (ID: " + ligneCommande.getId() + ") n'a pas de produit associé.");
            }
            if (ligneCommande.getPrixUnitaireTTC() == null) { // Vérifier le prix unitaire TTC de la ligne de commande
                throw new IllegalStateException("La ligne de commande pour le produit '" + ligneCommande.getProduit().getNom() + "' a des informations de prix unitaire manquantes.");
            }

            // Décrémenter le stock pour la quantité de la ligne de commande
            // Cette opération est CRITIQUE et doit se faire avant de créer la ligne BDS.
            // Si gestionStockService.decrementerStock lève une exception (stock insuffisant),
            // toute la transaction sera annulée, ce qui est le comportement souhaité.
            gestionStockService.decrementerStock(ligneCommande.getProduit(), ligneCommande.getQuantite());


            LigneBonDeSortie ligneBds = new LigneBonDeSortie();
            ligneBds.setBonDeSortie(bds); // Liaison bidirectionnelle
            ligneBds.setProduit(ligneCommande.getProduit());
            ligneBds.setQuantiteSortie(ligneCommande.getQuantite());
            ligneBds.setPrixUnitaireTTC(ligneCommande.getPrixUnitaireTTC()); // Le prix vient de la LigneCommande
            ligneBds.setQuantiteRetournee(0); // Initialisation explicite
            ligneBds.setQuantiteFacturee(0); // Initialisation explicite

            lignesBdsACreer.add(ligneBds);

            // Le calcul de la valeur totale doit se faire sur la quantité sortie, pas le total ligne commande si le prix unitaire change
            valeurTotaleInitialeTTC = valeurTotaleInitialeTTC.add(
                    ligneBds.getPrixUnitaireTTC().multiply(BigDecimal.valueOf(ligneBds.getQuantiteSortie()))
            );
        }
        bds.setLignes(lignesBdsACreer.stream().collect(Collectors.toSet())); // Convertir en Set pour l'entité
        bds.setValeurTotaleInitialeTTC(valeurTotaleInitialeTTC);


        BonDeSortie bdsSauvegarde = bonDeSortieRepository.save(bds);
        // Les LigneBonDeSortie sont sauvegardées en cascade si CascadeType.ALL est configuré sur BonDeSortie
        ligneBonDeSortieRepository.saveAll(lignesBdsACreer); // Sauvegarde explicite des lignes BDS pour s'assurer qu'elles ont un ID

        // 4. Mettre à jour le statut de la commande.
        commande.setStatutCommande(StatutCommande.EXPEDIEE); // La commande est EXPÉDIÉE si un BDS est créé
        commandeRepository.save(commande);

        return new BonDeSortieCreeDto(bdsSauvegarde);
    }

    @Transactional(readOnly = true)
    public List<BonDeSortieListDTO> getAllBonsDeSortie() {
        return bonDeSortieRepository.findAll()
                .stream()
                .map(BonDeSortieListDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void enregistrerRetours(Long bdsId, List<RetourProduitDTO> retours) {
        BonDeSortie bds = bonDeSortieRepository.findById(bdsId)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'ID : " + bdsId));

        if (bds.getStatut() == StatutBonDeSortie.ANNULE || bds.getStatut() == StatutBonDeSortie.FACTURE) { // Peut-être permettre les retours même après facturation complète ? A décider
            throw new IllegalStateException("Les retours ne peuvent être enregistrés que pour un bon de sortie dans un statut valide (ex: CRÉE, EN_COURS, PARTIELLEMENT_LIVRE).");
        }

        List<LigneBonDeSortie> lignesAMettreAJour = new ArrayList<>();

        for (RetourProduitDTO retour : retours) {
            LigneBonDeSortie ligne = ligneBonDeSortieRepository.findById(retour.getLigneBonDeSortieId())
                    .orElseThrow(() -> new EntityNotFoundException("Ligne de bon de sortie non trouvée avec l'ID : " + retour.getLigneBonDeSortieId()));

            if (!ligne.getBonDeSortie().getId().equals(bdsId)) {
                throw new IllegalStateException("Incohérence : La ligne de retour ID " + ligne.getId() + " n'appartient pas au bon de sortie ID " + bdsId);
            }
            // Vérifier que la quantité retournée n'est pas supérieure à ce qui reste à retourner
            if (retour.getQuantiteRetournee() > (ligne.getQuantiteSortie() - ligne.getQuantiteRetournee())) {
                throw new IllegalArgumentException("La quantité retournée (" + retour.getQuantiteRetournee() + ") pour le produit '" + ligne.getProduit().getNom() + "' est supérieure à la quantité restante à retourner ("+ (ligne.getQuantiteSortie() - ligne.getQuantiteRetournee()) +").");
            }

            ligne.setQuantiteRetournee(ligne.getQuantiteRetournee() + retour.getQuantiteRetournee()); // Ajouter la quantité au compteur de retour
            lignesAMettreAJour.add(ligne); // Ajouter pour sauvegarde

            // Réintégrer le stock.
            gestionStockService.reintegrerStock(ligne.getProduit().getId(), retour.getQuantiteRetournee());
        }

        ligneBonDeSortieRepository.saveAll(lignesAMettreAJour); // Sauvegarder toutes les lignes de BDS modifiées

        // Mettre à jour le statut du Bon de Sortie
        boolean tousRetoursEnregistres = bds.getLignes().stream()
                .allMatch(ligne -> ligne.getQuantiteRetournee() >= ligne.getQuantiteSortie()); // Toutes les quantités ont été retournées

        if (tousRetoursEnregistres) {
            bds.setStatut(StatutBonDeSortie.RETOURNE); // Tous les produits sont retournés ou non livrés
        } else {
            bds.setStatut(StatutBonDeSortie.PARTIELLEMENT_LIVRE); // Une partie est livrée, une partie retournée/non livrée
        }
        bonDeSortieRepository.save(bds);
    }

    @Transactional(readOnly = true)
    public BonDeSortieDetailDto getBonDeSortieDetails(Long bdsId) {
        BonDeSortie bds = bonDeSortieRepository.findById(bdsId)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'ID : " + bdsId));

        BonDeSortieDetailDto detailDto = new BonDeSortieDetailDto();

        detailDto.setId(bds.getId());
        detailDto.setNumeroBDS(bds.getNumeroBDS());
        detailDto.setDateSortie(bds.getDateSortie());
        detailDto.setStatut(bds.getStatut());
        detailDto.setValeurTotaleInitialeTTC(bds.getValeurTotaleInitialeTTC());

        if (bds.getChauffeur() != null) {
            detailDto.setChauffeurNom(bds.getChauffeur().getNom());
        }
        if (bds.getCommandeOrigine() != null) {
            detailDto.setCommandeOrigineId(bds.getCommandeOrigine().getId());
        }

        Set<LigneBonDeSortie> lignes = bds.getLignes();
        List<LigneBdsDetailDto> lignesDto = lignes.stream()
                .map(ligne -> new LigneBdsDetailDto(
                        ligne.getId(),
                        ligne.getProduit().getId(),
                        ligne.getProduit().getNom(),
                        ligne.getQuantiteSortie(),
                        ligne.getQuantiteRetournee(),
                        ligne.getQuantiteFacturee(), // <--- AJOUTE quantiteFacturee
                        ligne.getQuantiteDisponiblePourFacturation(), // <--- AJOUTE quantiteDisponiblePourFacturation
                        ligne.getPrixUnitaireTTC(),
                        ligne.getPrixUnitaireTTC().multiply(BigDecimal.valueOf(ligne.getQuantiteSortie() - ligne.getQuantiteRetournee())) // Prix total pour la quantité non retournée
                )).collect(Collectors.toList());
        detailDto.setLignes(lignesDto);

        Set<Facture> factures = bds.getFactures();
        List<FactureSimpleDto> facturesDto = factures.stream()
                .map(facture -> new FactureSimpleDto(
                        facture.getId(),
                        facture.getNumeroFacture(),
                        facture.getDateFacturation(),
                        facture.getTotalTTC(),
                        facture.getStatut()
                )).collect(Collectors.toList());
        detailDto.setFactures(facturesDto);

        return detailDto;
    }

    @Transactional(readOnly = true)
    public VerificationBDS_DTO getDetailsVerification(Long bdsId) {
        BonDeSortie bds = bonDeSortieRepository.findById(bdsId)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé"));

        BigDecimal valeurInitiale = bds.getValeurTotaleInitialeTTC() != null ? bds.getValeurTotaleInitialeTTC() : BigDecimal.ZERO;

        Set<Facture> factures = bds.getFactures();
        BigDecimal totalVendu = factures.stream()
                .filter(facture -> facture.getStatut() != StatutFacture.ANNULEE) // Ne pas inclure les factures annulées
                .map(Facture::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<LigneBonDeSortie> lignes = bds.getLignes();
        BigDecimal valeurRetour = lignes.stream()
                .filter(ligne -> ligne.getPrixUnitaireTTC() != null)
                .map(ligne -> ligne.getPrixUnitaireTTC().multiply(new BigDecimal(ligne.getQuantiteRetournee())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ecart = valeurInitiale.subtract(totalVendu).subtract(valeurRetour);
        String statutCoherence = ecart.abs().compareTo(new BigDecimal("0.01")) < 0 ? "COHERENT" : "INCOHERENT";

        return new VerificationBDS_DTO(bdsId, bds.getNumeroBDS(), valeurInitiale, totalVendu, valeurRetour, ecart, statutCoherence);
    }
    @Transactional(readOnly = true)
    public List<ProduitDTO> getProduitsDTOParBonDeSortie(Long bdsId) {
        BonDeSortie bds = bonDeSortieRepository.findById(bdsId)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'ID : " + bdsId));

        return bds.getLignes()
                .stream()
                .map(ligne -> new ProduitDTO(ligne.getProduit())) // ✅ Utilisation directe de ton constructeur
                .distinct()
                .collect(Collectors.toList());
    }



}