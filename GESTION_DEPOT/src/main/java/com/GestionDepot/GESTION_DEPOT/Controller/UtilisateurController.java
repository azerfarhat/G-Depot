package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Model.Utilisateur;
import com.GestionDepot.GESTION_DEPOT.Request.UtilisateurDto;
import com.GestionDepot.GESTION_DEPOT.Response.UtilisateurSimpleDto;
import com.GestionDepot.GESTION_DEPOT.Service.UtilisateurService;
import com.GestionDepot.GESTION_DEPOT.dto.ChauffeurDashboardDTO;
import com.GestionDepot.GESTION_DEPOT.dto.ChauffeurListDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/utilisateurs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    /**
     * Endpoint pour ajouter un nouvel utilisateur (y compris un chauffeur).
     * Les champs obligatoires sont validés par @Valid sur UtilisateurDto.
     */
    @PostMapping("/ajouter_utilisateurs")
    public ResponseEntity<?> ajouterUtilisateur(@Valid @RequestBody UtilisateurDto dto) {
        try {
            UtilisateurSimpleDto responseDto = utilisateurService.mapToDto(utilisateurService.ajouterUtilisateur(dto));
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Endpoint pour récupérer la liste de tous les chauffeurs.
     */
    @GetMapping("/chauffeurs")
    public List<ChauffeurListDto> getListeChauffeurs() {
        return utilisateurService.getChauffeurs();
    }

    /**
     * Endpoint pour supprimer un utilisateur par son ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUtilisateur(@PathVariable Long id) {
        try {
            utilisateurService.deleteUtilisateur(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès."));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erreur lors de la suppression de l'utilisateur: " + e.getMessage()));
        }
    }

    // --- Autres endpoints (inchangés) ---
    @GetMapping("/List_responsables")
    public List<UtilisateurSimpleDto> getResponsables() {
        return utilisateurService.getResponsablesDepot();
    }

    @GetMapping("/List_Utilisateur")
    public List<UtilisateurSimpleDto> getAll() {
        return utilisateurService.getAllUtilisateurs();
    }

    @GetMapping("/Get_User/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            UtilisateurSimpleDto dto = utilisateurService.getUtilisateurById(id);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ChauffeurDashboardDTO> getDashboardData(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getDashboardData(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurDto dto) {
        try {
            // Appelle le service pour effectuer la mise à jour
            Utilisateur utilisateurModifie = utilisateurService.updateUtilisateur(id, dto);
            // Retourne le DTO de la réponse
            UtilisateurSimpleDto responseDto = utilisateurService.mapToDto(utilisateurModifie);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            // Erreur si l'utilisateur n'est pas trouvé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Gère les autres erreurs (validation, etc.)
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

}