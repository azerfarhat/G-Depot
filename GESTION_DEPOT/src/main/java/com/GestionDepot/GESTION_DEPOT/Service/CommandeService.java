package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Response.UtilisateurSimpleDto;
import com.GestionDepot.GESTION_DEPOT.Model.*;
import com.GestionDepot.GESTION_DEPOT.Repository.*;
import com.GestionDepot.GESTION_DEPOT.Response.CommandeResponseDto;

import com.GestionDepot.GESTION_DEPOT.Response.LigneCommandeResponseDto;
import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockRepository stockRepository;

    public CommandeService(CommandeRepository commandeRepository, UtilisateurRepository utilisateurRepository, StockRepository stockRepository) {
        this.commandeRepository = commandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.stockRepository = stockRepository;
    }


    @Transactional
    public CommandeResponseDto creerCommandeVide(Long clientId) {
        Optional<Commande> commandeExistanteOpt = commandeRepository.findByClientIdAndStatutCommande(clientId, StatutCommande.VALIDEE);

        if (commandeExistanteOpt.isPresent()) {
            // Si le client a déja une commande en cour on le informer
            throw new IllegalStateException("Une commande en cours existe déjà pour le client ID : " + clientId);

        }
        // Le client n'a pas de panier ouvert, on en crée un nouveau.
        Utilisateur client = utilisateurRepository.findById(clientId)
                .filter(u -> u.getRole() == RoleUtilisateur.CHAUFFEUR)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur (client) introuvable avec l'ID : " + clientId));

        Commande nouvelleCommande = new Commande();
        nouvelleCommande.setClient(client);
        nouvelleCommande.setStatutCommande(StatutCommande.EN_COURS);
         Commande commandeSauvegardee = commandeRepository.save(nouvelleCommande);
        System.out.println("Info : Création d'une nouvelle commande pour le client ID " + clientId);
        return mapCommandeToDto(commandeSauvegardee);
    }

    public CommandeResponseDto getCommandeById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée avec l’ID : " + id));
        return mapCommandeToDto(commande);
    }

    public List<CommandeResponseDto> getToutesCommandes() {
        return commandeRepository.findAll().stream()
                .map(this::mapCommandeToDto)
                .collect(Collectors.toList());
    }

    public List<CommandeResponseDto> getCommandesParClient(Long clientId) {
        return commandeRepository.findByClientId(clientId).stream()
                .map(this::mapCommandeToDto)
                .collect(Collectors.toList());
    }

    public List<CommandeResponseDto> getCommandesParStatut(StatutCommande statut) {
        return commandeRepository.findByStatutCommande(statut).stream()
                .map(this::mapCommandeToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommandeResponseDto validerCommande(Long idCommande) {
        Commande commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée : " + idCommande));
        if (commande.getStatutCommande() != StatutCommande.EN_COURS) {
            throw new IllegalStateException("Seules les commandes en cours peuvent être validées.");
        }
        commande.setStatutCommande(StatutCommande.VALIDEE);
        return mapCommandeToDto(commandeRepository.save(commande));
    }

    @Transactional
    public void annulerCommande(Long idCommande) {
        Commande commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new EntityNotFoundException("Commande introuvable id: " + idCommande));

        if (commande.getStatutCommande() == StatutCommande.ANNULEE) return; // Déjà annulée

        // Restituer le stock
        for (LigneCommande ligne : commande.getLignes()) {
            for (RetraitStock retrait : ligne.getRetraits()) {
                retrait.getStock().setQuantiteProduit(retrait.getStock().getQuantiteProduit() + retrait.getQuantiteRetiree());
                retrait.getStock().updateStatut();
                stockRepository.save(retrait.getStock());
            }
        }
        commande.setStatutCommande(StatutCommande.ANNULEE);
        commandeRepository.save(commande);
    }

    @Transactional
    public void mettreAJourTotalCommande(Commande commande) {
        BigDecimal totalCommande = commande.getLignes().stream()
                .map(LigneCommande::getPrixVenteTotalLigneTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        commande.setTotaleCommandeTTC(totalCommande);
        commandeRepository.save(commande);
    }


    public CommandeResponseDto mapCommandeToDto(Commande commande) {
        if (commande == null) return null;
        CommandeResponseDto dto = new CommandeResponseDto();
        dto.setId(commande.getId());
        dto.setDateCommande(LocalDate.from(commande.getDateCommande()));
        dto.setStatutCommande(commande.getStatutCommande());
        dto.setTotaleCommandeTTC(commande.getTotaleCommandeTTC());

        if (commande.getClient() != null) {
            dto.setClient(mapUtilisateurToSimpleDto(commande.getClient()));
        }
        if (commande.getLignes() != null) {
            dto.setLignes(commande.getLignes().stream()
                    .map(this::mapLigneCommandeToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private LigneCommandeResponseDto mapLigneCommandeToDto(LigneCommande ligne) {
        LigneCommandeResponseDto dto = new LigneCommandeResponseDto();
        dto.setId(ligne.getId());
        dto.setQuantite(ligne.getQuantite());
        dto.setPrixVenteTotalLigneTTC(ligne.getPrixVenteTotalLigneTTC());
        if (ligne.getProduit() != null) {
            dto.setProduitId(ligne.getProduit().getId());
            dto.setNomProduit(ligne.getProduit().getNom());
        }
        return dto;
    }

    private UtilisateurSimpleDto mapUtilisateurToSimpleDto(Utilisateur utilisateur) {
        UtilisateurSimpleDto dto = new UtilisateurSimpleDto();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setEmail(utilisateur.getEmail());
        return dto;
    }
}