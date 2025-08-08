package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Model.Fournisseur;
import com.GestionDepot.GESTION_DEPOT.Repository.FournisseurRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.StockRepository;
import com.GestionDepot.GESTION_DEPOT.dto.FournisseurSummaryDto; // Import the DTO
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Autowired
    private StockRepository stockRepository;


    public Fournisseur ajouterFournisseur(Fournisseur fournisseur) {
        if (fournisseurRepository.existsByEmail(fournisseur.getEmail())) {
            throw new IllegalStateException("Un fournisseur avec cet email existe déjà.");
        }
        return fournisseurRepository.save(fournisseur);
    }
    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurRepository.findAll();
    }

    // NEW METHOD to get summary data
    public List<FournisseurSummaryDto> getAllFournisseurSummaries() {
        return fournisseurRepository.findAllFournisseurSummaries();
    }

    public Optional<Fournisseur> getFournisseurById(Long id) {
        return fournisseurRepository.findById(id);
    }

    @Transactional // Très recommandé pour les opérations de mise à jour pour garantir l'atomicité
    public Fournisseur mettreAJourFournisseur(Long id, Fournisseur fournisseurDetails) {
        // 1. Récupérer le fournisseur existant de la base de données
        Fournisseur existingFournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable avec l'ID: " + id));

        // 2. Mettre à jour les champs "obligatoires" directement.
        // Puisque le frontend pré-remplit le formulaire et envoie l'objet complet,
        // les champs non modifiés conserveront leur valeur originale et seront réaffectés à eux-mêmes.
        // Les champs modifiés prendront la nouvelle valeur.
        // Le frontend (avec Validators.required) est responsable de s'assurer qu'ils ne sont pas vides.
        existingFournisseur.setNom(fournisseurDetails.getNom());
        existingFournisseur.setSociete(fournisseurDetails.getSociete());
        existingFournisseur.setTelephone(fournisseurDetails.getTelephone());
        existingFournisseur.setAdresse(fournisseurDetails.getAdresse());
        existingFournisseur.setPays(fournisseurDetails.getPays());

        // 3. Gérer l'email avec une vérification d'unicité, seulement si l'email a changé.
        if (!Objects.equals(fournisseurDetails.getEmail(), existingFournisseur.getEmail())) {
            // Si le nouvel email n'est pas null/blanc ET qu'il existe déjà pour un autre fournisseur
            if (fournisseurDetails.getEmail() != null && !fournisseurDetails.getEmail().isBlank() &&
                    fournisseurRepository.existsByEmail(fournisseurDetails.getEmail())) {
                throw new IllegalStateException("Un autre fournisseur avec cet email existe déjà.");
            }
            existingFournisseur.setEmail(fournisseurDetails.getEmail());
        }

        // 4. Gérer les champs "optionnels" (comme siteWeb) :
        // Si la valeur reçue du frontend est null ou une chaîne vide, cela signifie que l'utilisateur veut la vider.
        // Dans ce cas, nous la mettons explicitement à null en base de données.
        // Sinon, nous la mettons à jour avec la nouvelle valeur.
        if (fournisseurDetails.getSiteWeb() == null || fournisseurDetails.getSiteWeb().isBlank()) {
            existingFournisseur.setSiteWeb(null); // Mettre à null en DB
        } else {
            existingFournisseur.setSiteWeb(fournisseurDetails.getSiteWeb()); // Mettre à jour avec la valeur envoyée
        }

        // 5. Sauvegarder l'entité mise à jour
        return fournisseurRepository.save(existingFournisseur);
    }

    @Transactional
    public void supprimerFournisseur(Long fournisseurId) {
        // On vérifie que le fournisseur existe.
        if (!fournisseurRepository.existsById(fournisseurId)) {
            throw new EntityNotFoundException("Fournisseur non trouvé avec l'ID : " + fournisseurId);
        }

        if (stockRepository.existsByFournisseurId(fournisseurId)) {
            // Si oui, on bloque la suppression et on envoie un message d'erreur clair.
            throw new IllegalStateException("Impossible de supprimer ce fournisseur car il est encore lié à des lots de stock existants.");
        }

        // Si la vérification passe, cela signifie qu'il n'y a aucun stock lié.
        fournisseurRepository.deleteById(fournisseurId);
    }
}