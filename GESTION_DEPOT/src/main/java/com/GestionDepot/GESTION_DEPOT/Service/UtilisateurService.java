package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Model.*;
import com.GestionDepot.GESTION_DEPOT.Repository.BonDeSortieRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.DepotRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.UtilisateurRepository;
import com.GestionDepot.GESTION_DEPOT.Request.UtilisateurDto;
import com.GestionDepot.GESTION_DEPOT.Response.UtilisateurSimpleDto;
import com.GestionDepot.GESTION_DEPOT.Response.VehiculeDto;
import com.GestionDepot.GESTION_DEPOT.dto.ChauffeurDashboardDTO;
import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import com.GestionDepot.GESTION_DEPOT.dto.ChauffeurListDto;
import com.GestionDepot.GESTION_DEPOT.Dto.DepotDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Utilise Lombok pour l'injection
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final DepotRepository depotRepository;
    private final EmailService emailService;
    private final BonDeSortieRepository bonDeSortieRepository; // Nouvelle dépendance

    /**
     * Ajoute un nouvel utilisateur (peut être un chauffeur, responsable, etc.).
     * Les validations sont gérées par @Valid dans le contrôleur et @NotBlank/@NotNull dans UtilisateurDto.
     */
    public Utilisateur ajouterUtilisateur(UtilisateurDto dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("L'adresse email '" + dto.getEmail() + "' est déjà utilisée.");
        }


        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setRole(dto.getRole());

        Depot depotAssigne = depotRepository.findById(dto.getDepotId())
                .orElseThrow(() -> new EntityNotFoundException("Dépôt non trouvé avec l'ID : " + dto.getDepotId()));
        utilisateur.setDepot(depotAssigne);



        if (dto.getRole() == RoleUtilisateur.CHAUFFEUR) {
            // Si le rôle est CHAUFFEUR, alors on vérifie les champs spécifiques.
            // On utilise StringUtils.hasText pour vérifier que la chaîne n'est ni nulle, ni vide, ni composée d'espaces.
            if (!StringUtils.hasText(dto.getNumeroPermis())) {
                throw new IllegalArgumentException("Le numéro de permis est obligatoire pour un chauffeur.");
            }
            if (!StringUtils.hasText(dto.getMarqueVehicule())) {
                throw new IllegalArgumentException("La marque du véhicule est obligatoire pour un chauffeur.");
            }
            if (!StringUtils.hasText(dto.getModeleVehicule())) {
                throw new IllegalArgumentException("Le modèle du véhicule est obligatoire pour un chauffeur.");
            }
            if (!StringUtils.hasText(dto.getMatriculeVehicule())) {
                throw new IllegalArgumentException("La matricule du véhicule est obligatoire pour un chauffeur.");
            }

            // Si tout est bon, on crée et on associe le véhicule.
            Vehicule nouveauVehicule = new Vehicule();
            nouveauVehicule.setMarque(dto.getMarqueVehicule());
            nouveauVehicule.setModele(dto.getModeleVehicule());
            nouveauVehicule.setMatricule(dto.getMatriculeVehicule());
            utilisateur.setTelephone(dto.getTelephone()); // Assigner le numéro de téléphone

            utilisateur.setNumeroPermis(dto.getNumeroPermis());
            utilisateur.setVehicule(nouveauVehicule);
        }

        Utilisateur savedUser = utilisateurRepository.save(utilisateur); // Sauvegarde l'utilisateur pour obtenir l'ID

        // Envoi de l'email de bienvenue
        try {
            String subject = "Bienvenue chez GESTION_DEPOT!";
            emailService.sendWelcomeEmail(savedUser.getEmail(), subject, savedUser.getNom(), savedUser.getEmail()); // Passez le nom et l'email
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }

        return savedUser;
    }
    // --- Méthodes de récupération des autres listes d'utilisateurs (utilisent UtilisateurSimpleDto) ---
    public List<UtilisateurSimpleDto> getResponsablesDepot() {
        return utilisateurRepository.findByRole(RoleUtilisateur.RESPONSABLE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UtilisateurSimpleDto> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UtilisateurSimpleDto getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    /**
     * Méthode de mapping pour convertir une entité Utilisateur en UtilisateurSimpleDto.
     * Utilisée pour les vues générales d'utilisateur.
     */
    public UtilisateurSimpleDto mapToDto(Utilisateur utilisateur) {
        if (utilisateur == null) return null;

        UtilisateurSimpleDto dto = new UtilisateurSimpleDto();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone()); // <-- AJOUTÉ
        dto.setNumeroPermis(utilisateur.getNumeroPermis()); // <-- AJOUTÉ
        dto.setRole(utilisateur.getRole()); // <-- AJOUTÉ

        if (utilisateur.getDepot() != null) {
            Depot depot = utilisateur.getDepot();
            DepotDto depotDto = new DepotDto();
            depotDto.setId(depot.getId());
            depotDto.setNom(depot.getNom());
            // add other fields if you want (e.g., ville)
            dto.setDepot(depotDto);
        }

        // Mapper les informations du véhicule si l'utilisateur a un véhicule
        if (utilisateur.getVehicule() != null) { // <-- AJOUTÉ
            VehiculeDto vehiculeDto = new VehiculeDto();
            vehiculeDto.setId(utilisateur.getVehicule().getId()); // Optionnel
            vehiculeDto.setMarque(utilisateur.getVehicule().getMarque());
            vehiculeDto.setModele(utilisateur.getVehicule().getModele());
            vehiculeDto.setMatricule(utilisateur.getVehicule().getMatricule());
            dto.setVehicule(vehiculeDto);
        } else {
            dto.setVehicule(null); // Assurez-vous que c'est explicitement null si pas de véhicule
        }

        return dto;
    }


    /**
     * Récupère la liste des chauffeurs.
     * OPTIMISÉ : Utilise la projection DTO directe depuis la méthode findAllChauffeursDto du Repository.
     * Le statut est maintenant complètement éliminé.
     */
    public List<ChauffeurListDto> getChauffeurs() {
        List<ChauffeurListDto> chauffeursDto = utilisateurRepository.findAllChauffeursDto();

        // Le champ 'statut' n'existe plus dans ChauffeurListDto, donc pas besoin de le définir ici.

        return chauffeursDto;
    }

    /**
     * Supprime un utilisateur par son ID.
     * @param id L'ID de l'utilisateur à supprimer.
     * @throws EntityNotFoundException si l'utilisateur n'est pas trouvé.
     */
    public void deleteUtilisateur(Long id) {
        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findById(id);
        if (utilisateurOptional.isEmpty()) {
            throw new EntityNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        utilisateurRepository.deleteById(id);
    }
    public ChauffeurDashboardDTO getDashboardData(Long chauffeurId) {
        Utilisateur chauffeur = utilisateurRepository.findById(chauffeurId)
                .orElseThrow(() -> new EntityNotFoundException("Chauffeur non trouvé"));

        List<BonDeSortie> bons = bonDeSortieRepository.findByChauffeurId(chauffeurId);

        BigDecimal totalPrix = BigDecimal.ZERO;
        BigDecimal totalVendu = BigDecimal.ZERO;
        BigDecimal totalRetourne = BigDecimal.ZERO;

        for (BonDeSortie bds : bons) {
            totalPrix = totalPrix.add(bds.getValeurTotaleInitialeTTC());
            for (Facture facture : bds.getFactures()) {
                totalVendu = totalVendu.add(facture.getTotalTTC());
            }
            for (LigneBonDeSortie ligne : bds.getLignes()) {
                BigDecimal valeurLigneRetournee = ligne.getPrixUnitaireTTC().multiply(BigDecimal.valueOf(ligne.getQuantiteRetournee()));
                totalRetourne = totalRetourne.add(valeurLigneRetournee);
            }
        }

        double tauxDeVente = (totalPrix.compareTo(BigDecimal.ZERO) > 0)
                ? totalVendu.divide(totalPrix, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        BigDecimal marge = totalVendu.subtract(totalPrix).add(totalRetourne);

        ChauffeurDashboardDTO.ChauffeurStatsDTO stats = ChauffeurDashboardDTO.ChauffeurStatsDTO.builder()
                .totalPrix(totalPrix) // Corrigé
                .totalVendu(totalVendu)
                .totalRetourne(totalRetourne)
                .margeBeneficiaire(marge)
                .tauxDeVente(tauxDeVente)
                .build();

        List<ChauffeurDashboardDTO.HistoriqueBdsDTO> historique = bons.stream()
                .map(this::mapToHistoriqueDto)
                .collect(Collectors.toList());

        return ChauffeurDashboardDTO.builder()
                .chauffeur(new ChauffeurDashboardDTO.ChauffeurInfoDTO(chauffeur.getId(), chauffeur.getNom()))
                .stats(stats)
                .historiqueBons(historique)
                .build();
    }

    // Méthode helper privée pour le mapping
    private ChauffeurDashboardDTO.HistoriqueBdsDTO mapToHistoriqueDto(BonDeSortie bds) {
        BigDecimal vendu = bds.getFactures().stream().map(Facture::getTotalTTC).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retourne = bds.getLignes().stream().map(l -> l.getPrixUnitaireTTC().multiply(BigDecimal.valueOf(l.getQuantiteRetournee()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal marge = vendu.subtract(bds.getValeurTotaleInitialeTTC()).add(retourne);

        return ChauffeurDashboardDTO.HistoriqueBdsDTO.builder()
                .numeroBDS(bds.getNumeroBDS())
                .dateSortie(bds.getDateSortie())
                .valeurInitiale(bds.getValeurTotaleInitialeTTC())
                .valeurVendue(vendu)
                .valeurRetournee(retourne)
                .marge(marge)
                .statut(bds.getStatut().name())
                .build();
    }


    @Transactional
    public Utilisateur updateUtilisateur(Long id, UtilisateurDto dto) {
        // 1. On cherche l'utilisateur à modifier.
        Utilisateur utilisateurAModifier = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'ID : " + id));

        // 2. On vérifie si le nouvel email n'est pas déjà pris par un AUTRE utilisateur.
        utilisateurRepository.findByEmail(dto.getEmail()).ifPresent(utilisateurExistant -> {
            if (!utilisateurExistant.getId().equals(id)) {
                throw new IllegalStateException("L'email '" + dto.getEmail() + "' est déjà utilisé par un autre compte.");
            }
        });

        // 3. On met à jour les champs simples.
        utilisateurAModifier.setNom(dto.getNom());
        utilisateurAModifier.setEmail(dto.getEmail());
        utilisateurAModifier.setTelephone(dto.getTelephone());

        // On ne change le rôle que si nécessaire, mais ici on le met à jour.
        utilisateurAModifier.setRole(dto.getRole());

        // 4. On met à jour le mot de passe SEULEMENT s'il a été fourni.
        if (StringUtils.hasText(dto.getMotDePasse())) {
            utilisateurAModifier.setMotDePasse(dto.getMotDePasse());
        }

        // 5. On gère la mise à jour du véhicule.
        if (dto.getRole() == RoleUtilisateur.CHAUFFEUR) {
            // Si le rôle est CHAUFFEUR, alors on vérifie que les champs du véhicule sont présents.
            if (!StringUtils.hasText(dto.getNumeroPermis()) || !StringUtils.hasText(dto.getMarqueVehicule()) || !StringUtils.hasText(dto.getModeleVehicule()) || !StringUtils.hasText(dto.getMatriculeVehicule())) {
                throw new IllegalArgumentException("Les informations de permis et de véhicule sont obligatoires pour un chauffeur.");
            }

            Vehicule vehicule = utilisateurAModifier.getVehicule();
            if (vehicule == null) {
                // Si le chauffeur n'avait pas de véhicule, on en crée un nouveau.
                vehicule = new Vehicule();
            }

            // Mise à jour des informations du véhicule
            vehicule.setMarque(dto.getMarqueVehicule());
            vehicule.setModele(dto.getModeleVehicule());
            vehicule.setMatricule(dto.getMatriculeVehicule());
            utilisateurAModifier.setVehicule(vehicule);
            utilisateurAModifier.setNumeroPermis(dto.getNumeroPermis());

        } else {
            // Si l'utilisateur n'est plus un chauffeur, on supprime son véhicule
            utilisateurAModifier.setVehicule(null);
            utilisateurAModifier.setNumeroPermis(null);
        }

        // 6. On sauvegarde les modifications.
        return utilisateurRepository.save(utilisateurAModifier);
    }
}