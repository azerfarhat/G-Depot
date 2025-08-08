package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Request.ProduitUpdateDTO;
import com.GestionDepot.GESTION_DEPOT.Model.Produit;
import com.GestionDepot.GESTION_DEPOT.Repository.ProduitRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.StockRepository;
import com.GestionDepot.GESTION_DEPOT.dto.ProduitListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class GestionProduitService {

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private StockRepository stockRepository;

    public Produit creerProduit(Produit produit) {
        return produitRepository.save(produit);
    }

    public Optional<Produit> trouverProduitParId(Long id) {
        return produitRepository.findById(id);
    }


    public List<ProduitListDTO> listerProduitsPourVueListe() {
        return produitRepository.findProduitListDTOs();
    }

    public Produit mettreAJourProduit(Long id, ProduitUpdateDTO dto) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produit avec id " + id + " introuvable"));

        if (dto.getNom() != null && !dto.getNom().isBlank() && !dto.getNom().equals(produit.getNom())) {
            produit.setNom(dto.getNom());
        }

        if (dto.getDescription() != null && !dto.getDescription().isBlank() && !dto.getDescription().equals(produit.getDescription())) {
            produit.setDescription(dto.getDescription());
        }

        if (dto.getStrategieStock() != null && !dto.getStrategieStock().equals(produit.getStrategieStock())) {
            produit.setStrategieStock(dto.getStrategieStock());
        }

        return produitRepository.save(produit);
    }

    public boolean supprimerProduit(Long id) {
        //On vérifie que le produit existe.
        if (!produitRepository.existsById(id)) {
            return false;
        }

        //On vérifie s'il existe des stocks pour ce produit.
        if (stockRepository.existsByProduitId(id)) {
            // Si oui, on bloque l'opération et on envoie un message d'erreur clair.
            throw new IllegalStateException("Impossible de supprimer ce produit car il est encore présent en stock.");
        }

        //Si les vérifications passent, on peut supprimer le produit en toute sécurité.
        produitRepository.deleteById(id);
        return true;
    }

    public List<ProduitListDTO> rechercherProduits(String terme) {
        if (terme == null || terme.isBlank()) {
            return produitRepository.findProduitListDTOs(); // Si la recherche est vide, renvoie tout
        }
        return produitRepository.searchProduitListDTOs(terme); // Sinon, lance la recherche
    }

}


