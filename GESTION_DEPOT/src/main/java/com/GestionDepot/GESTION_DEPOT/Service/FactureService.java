package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Model.*;
import com.GestionDepot.GESTION_DEPOT.Repository.BonDeSortieRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.CommandeRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.FactureRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.ProduitRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.LigneBonDeSortieRepository; // NOUVEL IMPORT
import com.GestionDepot.GESTION_DEPOT.Request.FactureCreateRequestDto;
import com.GestionDepot.GESTION_DEPOT.Response.FactureResponseDto;
import com.GestionDepot.GESTION_DEPOT.Response.UtilisateurSimpleDto;
import com.GestionDepot.GESTION_DEPOT.dto.FactureCreateDTO;
import com.GestionDepot.GESTION_DEPOT.Request.LigneFactureCreateDTO;
import com.GestionDepot.GESTION_DEPOT.dto.FactureDTO;
import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture;
import com.GestionDepot.GESTION_DEPOT.enums.StatutBonDeSortie; // NOUVEL IMPORT
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream; // <-- NOUVEL IMPORT
import java.io.IOException; // <-- NOUVEL IMPORT
import org.thymeleaf.TemplateEngine; // <-- NOUVEL IMPORT
import org.thymeleaf.context.Context; // <-- NOUVEL IMPORT
import org.xhtmlrenderer.pdf.ITextRenderer; // <-- NOUVEL IMPORT pour OpenPDF
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FactureService {

    private final FactureRepository factureRepository;
    private final CommandeRepository commandeRepository;
    private final BonDeSortieRepository bonDeSortieRepository;
    private final ProduitRepository produitRepository ;
    private final GestionStockService gestionStockService;
    private final TemplateEngine templateEngine; // <-- NOUVELLE DÉPENDANCE
    private final LigneBonDeSortieRepository ligneBonDeSortieRepository; // <--- NOUVELLE DÉPENDANCE

    public FactureService(FactureRepository factureRepository, GestionStockService gestionStockService,
                          ProduitRepository produitRepository, CommandeRepository commandeRepository,
                          BonDeSortieRepository bonDeSortieRepository,
                          LigneBonDeSortieRepository ligneBonDeSortieRepository,
                          TemplateEngine templateEngine) { // <-- AJOUT AU CONSTRUCTEUR
        this.factureRepository = factureRepository;
        this.commandeRepository = commandeRepository;
        this.bonDeSortieRepository = bonDeSortieRepository;
        this.produitRepository = produitRepository;
        this.gestionStockService = gestionStockService;
        this.ligneBonDeSortieRepository = ligneBonDeSortieRepository;
        this.templateEngine = templateEngine; // <-- ASSIGNATION
    }

    @Transactional
    public FactureDTO creerFacturePourChauffeur(FactureCreateDTO dto) {
        BonDeSortie bds = bonDeSortieRepository.findById(dto.getBonDeSortieId())
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'id : " + dto.getBonDeSortieId()));

        // Vérifier le statut du Bon de Sortie avant de le facturer
        if (bds.getStatut() == StatutBonDeSortie.ANNULE || bds.getStatut() == StatutBonDeSortie.FACTURE) {
            throw new IllegalStateException("Impossible de facturer un Bon de Sortie avec le statut : " + bds.getStatut());
        }

        Facture facture = new Facture();
        facture.setBonDeSortie(bds);
        facture.setNumeroFacture("FACT-CH-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 5));
        facture.setDateFacturation(LocalDate.now());
        facture.setDateEcheance(LocalDate.now());
        facture.setStatut(StatutFacture.EMISE); // Statut initial à EmiSE, pas PAYEE
        // Assurez-vous que le dépôt est défini pour le calcul de la TVA
        if (bds.getCommandeOrigine() != null && bds.getCommandeOrigine().getDepot() != null) {
            facture.setDepot(bds.getCommandeOrigine().getDepot());
        } else if (bds.getChauffeur() != null && bds.getChauffeur().getDepot() != null) {
            facture.setDepot(bds.getChauffeur().getDepot());
        } else {
            throw new IllegalStateException("Impossible de créer la facture : Dépôt non associé au Bon de Sortie ou au Chauffeur.");
        }


        BigDecimal totalFactureTTC = BigDecimal.ZERO;
        List<LigneBonDeSortie> lignesBdsAMettreAJour = new ArrayList<>(); // Pour stocker les lignes BDS à sauvegarder

        for (LigneFactureCreateDTO ligneDto : dto.getLignes()) {
            Produit produit = produitRepository.findById(ligneDto.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'id : " + ligneDto.getProduitId()));

            LigneBonDeSortie ligneBds = bds.getLignes().stream()
                    .filter(lbds -> lbds.getProduit().getId().equals(produit.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Le produit " + produit.getNom() + " n'existe pas dans le Bon de Sortie " + bds.getNumeroBDS()));

            int quantiteDemandee = ligneDto.getQuantite();
            if (quantiteDemandee <= 0) {
                throw new IllegalArgumentException("La quantité à facturer pour le produit " + produit.getNom() + " doit être positive.");
            }
            if (quantiteDemandee > ligneBds.getQuantiteDisponiblePourFacturation()) {
                throw new IllegalStateException("Quantité à facturer (" + quantiteDemandee + ") pour le produit " + produit.getNom() + " dépasse la quantité disponible dans le Bon de Sortie (" + ligneBds.getQuantiteDisponiblePourFacturation() + ").");
            }

            // La quantité qui sera facturée est celle demandée, et elle est déduite du BDS
            ligneBds.setQuantiteFacturee(ligneBds.getQuantiteFacturee() + quantiteDemandee);
            lignesBdsAMettreAJour.add(ligneBds); // Marque cette ligne BDS pour sauvegarde

            // Créer la LigneFacture
            LigneFacture ligneFacture = new LigneFacture();
            ligneFacture.setFacture(facture);
            ligneFacture.setProduit(produit);
            ligneFacture.setQuantite(quantiteDemandee); // La quantité facturée est celle demandée ici
            ligneFacture.setPrixUnitaireTTC(ligneBds.getPrixUnitaireTTC()); // Utilisez le prix unitaire du BDS

            BigDecimal totalLigne = ligneFacture.getPrixUnitaireTTC().multiply(new BigDecimal(quantiteDemandee));
            ligneFacture.setTotalLigneTTC(totalLigne);
            facture.getLignes().add(ligneFacture);

            totalFactureTTC = totalFactureTTC.add(totalLigne);
        }

        // Finaliser les totaux de la facture (TTC, HT, TVA)
        facture.setTotalTTC(totalFactureTTC);
        if (facture.getDepot() != null && facture.getDepot().getTva() != null) {
            BigDecimal tvaRateDivisor = BigDecimal.ONE.add(facture.getDepot().getTva().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            BigDecimal totalHT = totalFactureTTC.divide(tvaRateDivisor, 2, RoundingMode.HALF_UP);
            BigDecimal montantTVA = totalFactureTTC.subtract(totalHT);
            facture.setTotalHT(totalHT);
            facture.setMontantTVA(montantTVA);
        } else {
            facture.setTotalHT(totalFactureTTC);
            facture.setMontantTVA(BigDecimal.ZERO);
            System.err.println("WARN: Dépôt ou taux de TVA non trouvé pour le calcul HT/TVA de la facture " + facture.getNumeroFacture());
        }

        // Sauvegarder l'entité Facture (les LigneFacture seront en cascade)
        Facture factureSauvegardee = factureRepository.save(facture);

        // Sauvegarder les LignesBonDeSortie modifiées
        ligneBonDeSortieRepository.saveAll(lignesBdsAMettreAJour);

        // Mettre à jour le statut du Bon de Sortie si toutes les quantités sont facturées
        boolean toutesFacturees = bds.getLignes().stream()
                .allMatch(ligne -> ligne.getQuantiteDisponiblePourFacturation() == 0);

        if (toutesFacturees) {
            bds.setStatut(StatutBonDeSortie.FACTURE); // Marquer le BDS comme entièrement facturé
        } else {
            bds.setStatut(StatutBonDeSortie.PARTIELLEMENT_FACTURE); // Si non, partiellement facturé
        }
        bonDeSortieRepository.save(bds); // Sauvegarder le BDS avec le nouveau statut

        // Transformer l'entité sauvegardée en DTO de réponse PENDANT la transaction
        return new FactureDTO(factureSauvegardee);
    }

    @Transactional
    public FactureResponseDto  creerFacturePourCommande(Long commandeId, FactureCreateRequestDto requestDto) {
        // ... (votre code existant, il n'y a pas de changement direct ici pour la logique de quantité BDS)
        // Note : Ici, la facturation se base sur les LigneCommande, et non les LigneBonDeSortie.
        // Si la commande est facturée, on peut assumer que le stock a déjà été déduit au moment
        // de la validation de la commande ou de la création du bon de sortie si c'est le flux.
        if (requestDto.getDateEcheance() == null) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire dans le corps de la requête.");
        }
        if (requestDto.getDateEcheance().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date d'échéance ne peut pas être dans le passé.");
        }

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l'ID : " + commandeId));

        if (commande.getStatutCommande() != StatutCommande.VALIDEE) {
            throw new IllegalStateException("Une facture ne peut être créée que pour une commande qui est au statut VALIDEE.");
        }

        if (factureRepository.existsByCommandeId(commandeId)) {
            throw new IllegalStateException("Une facture existe déjà pour la commande ID : " + commandeId);
        }

        Facture factureACreer = new Facture();
        factureACreer.setCommande(commande);
        factureACreer.setDateFacturation(LocalDate.now());
        factureACreer.setDateEcheance(requestDto.getDateEcheance());
        factureACreer.setStatut(StatutFacture.EMISE);
        factureACreer.setNumeroFacture(genererNumeroFacture());

        BigDecimal totalTTC = commande.getTotaleCommandeTTC();
        if (totalTTC == null) {
            throw new IllegalStateException("Impossible de créer une facture : le total TTC de la commande est nul.");
        }
        Set<LigneCommande> lignesCommande = commande.getLignes();

        if (!lignesCommande.isEmpty()) {
            LigneCommande uneLigne = lignesCommande.iterator().next();
            if (uneLigne.getRetraits() != null && !uneLigne.getRetraits().isEmpty()) {
                if (uneLigne.getRetraits().iterator().next().getStock() != null) {
                    factureACreer.setDepot(uneLigne.getRetraits().iterator().next().getStock().getDepot());
                }
            }
        }
        if (factureACreer.getDepot() == null && commande.getDepot() != null) { // Fallback au dépôt de la commande
            factureACreer.setDepot(commande.getDepot());
        } else if (factureACreer.getDepot() == null) { // Si pas de dépôt du tout
            throw new IllegalStateException("Impossible de créer la facture : Dépôt non associé à la commande ou ses lignes.");
        }


        // Calcul TVA basé sur le dépôt de la commande
        if (factureACreer.getDepot() != null && factureACreer.getDepot().getTva() != null) {
            BigDecimal tvaRateDivisor = BigDecimal.ONE.add(factureACreer.getDepot().getTva().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            BigDecimal totalHT = totalTTC.divide(tvaRateDivisor, 2, RoundingMode.HALF_UP);
            BigDecimal montantTVA = totalTTC.subtract(totalHT);
            factureACreer.setTotalHT(totalHT);
            factureACreer.setMontantTVA(montantTVA);
        } else {
            factureACreer.setTotalHT(totalTTC);
            factureACreer.setMontantTVA(BigDecimal.ZERO);
            System.err.println("WARN: Dépôt ou taux de TVA non trouvé pour le calcul HT/TVA de la facture " + factureACreer.getNumeroFacture());
        }

        factureACreer.setTotalTTC(totalTTC);
        // CRÉATION DES LIGNES DE FACTURE BASÉES SUR LES LIGNES DE COMMANDE
        for (LigneCommande lc : commande.getLignes()) {
            LigneFacture lf = new LigneFacture();
            lf.setFacture(factureACreer);
            lf.setProduit(lc.getProduit());
            lf.setQuantite(lc.getQuantite());
            lf.setPrixUnitaireTTC(lc.getPrixUnitaireTTC());
            lf.setTotalLigneTTC(lc.getPrixVenteTotalLigneTTC());
            factureACreer.getLignes().add(lf); // Ajouter la ligne de facture à la facture
        }


        Facture factureSauvegardee = factureRepository.save(factureACreer);

        commande.setStatutCommande(StatutCommande.FACTUREE);
        commandeRepository.save(commande);

        return mapFactureToDto(factureSauvegardee);
    }

    public FactureResponseDto getFactureByCommandeId(Long commandeId) {
        Facture facture = factureRepository.findByCommandeId(commandeId)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée pour la commande ID : " + commandeId));
        return mapFactureToDto(facture);
    }

    public FactureResponseDto getFactureByBonDeSortieId(Long bonDeSortieId) {
        Facture facture = factureRepository.findByBonDeSortieId(bonDeSortieId)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée pour le Bon de Sortie ID : " + bonDeSortieId));
        return mapFactureToDto(facture);
    }

    public List<FactureResponseDto> getFacturesByChauffeurId(Long chauffeurId) {
        List<Facture> factures = factureRepository.findByBonDeSortie_Chauffeur_Id(chauffeurId);
        return factures.stream()
                .map(this::mapFactureToDto)
                .collect(Collectors.toList());
    }


    public FactureResponseDto getFactureById(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée avec l'ID : " + id));
        return mapFactureToDto(facture);
    }

    public List<FactureResponseDto> getToutesFactures() {
        return factureRepository.findAll().stream()
                .map(this::mapFactureToDto)
                .collect(Collectors.toList());
    }

    public List<FactureResponseDto> getFacturesParClientId(Long clientId) {
        List<Commande> commandesDuClient = commandeRepository.findByClientId(clientId);

        if (commandesDuClient.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> commandeIds = commandesDuClient.stream()
                .map(Commande::getId)
                .collect(Collectors.toList());

        return factureRepository.findByCommandeIdIn(commandeIds).stream()
                .map(this::mapFactureToDto)
                .collect(Collectors.toList());
    }

    private String genererNumeroFacture() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = factureRepository.count() + 1;
        return "FAC-" + datePart + "-" + String.format("%04d", count);
    }

    @Transactional
    public FactureResponseDto updateStatutFacture(Long factureId, StatutFacture nouveauStatut) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée avec l'ID : " + factureId));

        if (facture.getStatut() == StatutFacture.ANNULEE) {
            throw new IllegalStateException("Impossible de modifier le statut d'une facture déjà annulée.");
        }
        if (facture.getStatut() == StatutFacture.PAYEE && nouveauStatut != StatutFacture.ANNULEE) {
            throw new IllegalStateException("Une facture déjà payée ne peut être qu'annulée (pour un remboursement).");
        }

        if (nouveauStatut == StatutFacture.ANNULEE) {
            Commande commandeAssociee = facture.getCommande();
            if (commandeAssociee != null) {
                commandeAssociee.setStatutCommande(StatutCommande.ANNULEE);
                commandeRepository.save(commandeAssociee);
            }
            if (facture.getBonDeSortie() != null) {
                // Si le BDS était complètement facturé, et que la facture est annulée,
                // on pourrait vouloir réouvrir le BDS (passer de FACTURE à PARTIELLEMENT_FACTURE ou LIVRE)
                BonDeSortie bds = facture.getBonDeSortie();
                // Il faudrait aussi annuler la quantité facturée sur les LigneBonDeSortie concernées
                // (si la facture était la seule à impacter ces quantités).
                // Pour la démo, on ne gère pas ce rollback complexe ici, mais c'est à considérer.
            }
        }

        facture.setStatut(nouveauStatut);
        Facture factureMiseAJour = factureRepository.save(facture);
        return mapFactureToDto(factureMiseAJour);
    }

    private FactureResponseDto mapFactureToDto(Facture facture) {
        if (facture == null) return null;

        FactureResponseDto dto = new FactureResponseDto();
        dto.setId(facture.getId());
        dto.setNumeroFacture(facture.getNumeroFacture());
        dto.setDateFacturation(facture.getDateFacturation());
        dto.setDateEcheance(facture.getDateEcheance());
        dto.setStatut(facture.getStatut());
        dto.setTotalTTC(facture.getTotalTTC());
        dto.setTotalHT(facture.getTotalHT());
        dto.setMontantTVA(facture.getMontantTVA());


        if (facture.getCommande() != null) {
            dto.setCommandeId(facture.getCommande().getId());
            if (facture.getCommande().getClient() != null) {
                dto.setClient(mapUtilisateurToSimpleDto(facture.getCommande().getClient()));
            }
        }

        if (facture.getBonDeSortie() != null) {
            dto.setBonDeSortieId(facture.getBonDeSortie().getId());
            if (facture.getBonDeSortie().getChauffeur() != null) {
                dto.setChauffeur(mapUtilisateurToSimpleDto(facture.getBonDeSortie().getChauffeur()));
            }
        }

        return dto;
    }

    // --- NOUVELLE MÉTHODE : Générer le PDF de la facture ---
    public byte[] generateFacturePdf(Long factureId) throws IOException { // Ajout de throws IOException
        // 1. Récupérer la facture complète (entité Facture)
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new EntityNotFoundException("Facture non trouvée avec l'ID : " + factureId));

        // 2. Mapper l'entité Facture en DTO pour le template (si votre template utilise le DTO)
        // Ou passer directement l'entité si le template est configuré pour l'entité.
        // Ici, nous allons mapper pour s'assurer que toutes les données sont bien structurées
        // et inclure les lignes de facture complètes.
        FactureResponseDto factureDto = mapFactureToDto(facture);
        // Assurez-vous que mapFactureToDto inclut les lignes de facture avec produit.nom, quantite, etc.
        // Si LigneFactureResponseDto n'existe pas, vous devrez l'ajouter ou adapter le template
        // pour utiliser directement les entités LigneFacture si elles sont chargées par défaut.
        // Pour cet exemple, je vais supposer que FactureResponseDto a une liste de LigneFactureResponseDto
        // et que mapFactureToDto les mappe correctement. Sinon, adaptez le DTO ou le mapping.

        // 3. Préparer le contexte Thymeleaf
        Context context = new Context();
        context.setVariable("facture", factureDto); // Passer le DTO de la facture au template

        // 4. Traiter le template HTML avec Thymeleaf
        String htmlContent = templateEngine.process("facture_template", context);

        // 5. Convertir le contenu HTML en PDF avec OpenPDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout(); // Important : effectue le layout du document
        renderer.createPDF(outputStream); // Génère le PDF

        return outputStream.toByteArray(); // Retourne le tableau d'octets du PDF
    }


    private UtilisateurSimpleDto mapUtilisateurToSimpleDto(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        UtilisateurSimpleDto dto = new UtilisateurSimpleDto();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setEmail(utilisateur.getEmail());
        return dto;
    }
}