package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Model.Localisation;
import com.GestionDepot.GESTION_DEPOT.Model.Utilisateur;
import com.GestionDepot.GESTION_DEPOT.Repository.LocalisationRepository;
import com.GestionDepot.GESTION_DEPOT.Repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/localisations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocalisationController {

    private final LocalisationRepository localisationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    public List<Localisation> getAll() {
        return localisationRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Localisation localisation) {
        if ("Client".equals(localisation.getType()) && localisation.getClient() != null) {
            Utilisateur client = utilisateurRepository.findById(localisation.getClient().getId())
                    .orElse(null);
            localisation.setClient(client);
        } else {
            localisation.setClient(null);
        }
        return ResponseEntity.ok(localisationRepository.save(localisation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        localisationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}