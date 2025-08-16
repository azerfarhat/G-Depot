// package com.GestionDepot.GESTION_DEPOT.Service;
// MODIFIÉ
package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Response.UtilisateurSimpleDto;
import com.GestionDepot.GESTION_DEPOT.Model.Depot;
import com.GestionDepot.GESTION_DEPOT.Model.Utilisateur;
import com.GestionDepot.GESTION_DEPOT.Model.Facture;
import com.GestionDepot.GESTION_DEPOT.Repository.DepotRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.UtilisateurRepository;
import com.GestionDepot.GESTION_DEPOT.Dto.DepotDto;
import com.GestionDepot.GESTION_DEPOT.Response.VehiculeDto;
import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepotService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Transactional
    public void modifierTvaDepot(Long id, BigDecimal nouvelleTva) {
        Depot depot = getDepotById(id);
        depot.setTva(nouvelleTva);
        depotRepository.save(depot);
    }

    @Transactional
    public Depot ajouterDepot(Depot depot, Long responsableId) {
        Utilisateur responsable = utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new RuntimeException("Utilisateur (responsable) introuvable avec l'ID: " + responsableId));

        if (responsable.getRole() != RoleUtilisateur.RESPONSABLE) {
            throw new IllegalStateException("L'utilisateur avec l'ID " + responsableId + " n'a pas le rôle de RESPONSABLE et ne peut pas être assigné à un dépôt.");
        }

        depot.setResponsable(responsable);
        return depotRepository.save(depot);
    }

    // Cette méthode peut rester pour les cas où l'entité brute est nécessaire en interne
    public List<Depot> getAllDepots() {
        return depotRepository.findAll();
    }

    // Cette méthode peut rester pour les cas où l'entité brute est nécessaire en interne
    public List<Depot> getDepotsParResponsable(Long responsableId) {
        Utilisateur responsable = utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new RuntimeException("Responsable introuvable avec l'ID: " + responsableId));
        return depotRepository.findByResponsable(responsable);
    }

    // NOUVELLE MÉTHODE : pour obtenir les dépôts par responsable sous forme de DTO
    @Transactional(readOnly = true)
    public List<DepotDto> getDepotsParResponsableAsDto(Long responsableId) {
        Utilisateur responsable = utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new RuntimeException("Responsable introuvable avec l'ID: " + responsableId));
        List<Depot> depots = depotRepository.findByResponsable(responsable);
        return depots.stream()
                .map(this::convertToDepotDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void supprimerDepot(Long id) {
        depotRepository.deleteById(id);
    }

    // Cette méthode peut rester pour les cas où l'entité brute est nécessaire en interne
    @Transactional(readOnly = true)
    public Depot getDepotById(Long id) {
        return depotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dépôt non trouvé avec id: " + id));
    }

    // NOUVELLE MÉTHODE : pour obtenir un seul dépôt sous forme de DTO
    @Transactional(readOnly = true)
    public DepotDto getDepotDtoById(Long id) {
        Depot depot = depotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dépôt non trouvé avec id: " + id));
        return convertToDepotDto(depot);
    }

    // NOUVELLE MÉTHODE AJOUTÉE POUR LA MISE À JOUR CÔTÉ BACKEND
    @Transactional
    public Depot updateDepot(Long id, Depot updatedDepot) {
        Depot existingDepot = depotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dépôt non trouvé avec id: " + id));

        // Mettre à jour les champs pertinents
        existingDepot.setNom(updatedDepot.getNom());
        existingDepot.setAdresse(updatedDepot.getAdresse());
        existingDepot.setTelephone(updatedDepot.getTelephone());
        existingDepot.setTva(updatedDepot.getTva());

        // RÉINTRODUIRE LA MISE À JOUR POUR CES CHAMPS
        existingDepot.setVille(updatedDepot.getVille());
        existingDepot.setCodePostal(updatedDepot.getCodePostal());
        existingDepot.setZone(updatedDepot.getZone());
        existingDepot.setEmail(updatedDepot.getEmail());
        // Pas de mise à jour pour 'responsable' ici car c'est géré par assignerResponsableAuDepot

        return depotRepository.save(existingDepot);
    }


    @Transactional
    public void assignerResponsableAuDepot(Long idUtilisateur, Long idDepot) {
        Depot depot = depotRepository.findById(idDepot)
                .orElseThrow(() -> new IllegalArgumentException("Le dépôt avec ID " + idDepot + " n'existe pas"));

        Utilisateur responsable = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("L'utilisateur avec ID " + idUtilisateur + " n'existe pas"));

        if (responsable.getRole() != RoleUtilisateur.RESPONSABLE) {
            throw new IllegalStateException("L'utilisateur n'est pas un responsable de dépôt");
        }

        depot.setResponsable(responsable);
        depotRepository.save(depot);
    }

    @Transactional(readOnly = true)
    public List<DepotDto> getAllDepotsWithCalculatedCa() {
        List<Depot> depots = depotRepository.findAll();
        return depots.stream()
                .map(this::convertToDepotDto)
                .collect(Collectors.toList());
    }

    // Méthode utilitaire pour convertir un Depot en DepotDto
    // Mise à jour pour mapper les nouveaux champs
    private DepotDto convertToDepotDto(Depot depot) {
        DepotDto dto = new DepotDto();
        dto.setId(depot.getId());
        dto.setNom(depot.getNom());
        dto.setAdresse(depot.getAdresse());
        dto.setTelephone(depot.getTelephone());
        dto.setTva(depot.getTva());
        dto.setVille(depot.getVille());
        dto.setCodePostal(depot.getCodePostal());
        dto.setZone(depot.getZone());
        dto.setEmail(depot.getEmail());

        // Calcul du chiffre d'affaires
        BigDecimal chiffreAffaires = depot.getFactures().stream()
                .map(Facture::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setChiffreAffaires(chiffreAffaires);

        // Mapping du responsable
        if (depot.getResponsable() != null) {
            Utilisateur responsable = depot.getResponsable();
            UtilisateurSimpleDto responsableDto = new UtilisateurSimpleDto(
                    responsable.getId(),
                    responsable.getNom(),
                    responsable.getEmail(),
                    responsable.getTelephone(),
                    responsable.getNumeroPermis(),
                    (responsable.getVehicule() != null) ?
                            new VehiculeDto(
                                    responsable.getVehicule().getId(),
                                    responsable.getVehicule().getMarque(),
                                    responsable.getVehicule().getModele(),
                                    responsable.getVehicule().getMatricule()
                            ) : null,
                    responsable.getRole(),
                    null // Ajoutez ce champ si UtilisateurSimpleDto a un champ depot
            );
            dto.setResponsable(responsableDto);
        }

        return dto;
    }
}