package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Request.LigneCommandeRequestDto;
import com.GestionDepot.GESTION_DEPOT.Service.CommandeService;
import com.GestionDepot.GESTION_DEPOT.Service.LigneCommandeGestionService;
import com.GestionDepot.GESTION_DEPOT.Response.CommandeResponseDto;
import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
@RestController
public class CommandeController {

    private final CommandeService commandeService;
    private final LigneCommandeGestionService ligneCommandeGestionService;

    public CommandeController(CommandeService commandeService,
                              LigneCommandeGestionService ligneCommandeGestionService) {
        this.commandeService = commandeService;
        this.ligneCommandeGestionService = ligneCommandeGestionService;
    }

//commande passer seuelement pour les chauffeurs
    @PostMapping("/creer/{clientId}")
    public ResponseEntity<CommandeResponseDto> creerCommandePourClient(@PathVariable Long clientId) {
        CommandeResponseDto nouvelleCommande = commandeService.creerCommandeVide(clientId);
        return new ResponseEntity<>(nouvelleCommande, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CommandeResponseDto>> getToutesCommandes() {
        return ResponseEntity.ok(commandeService.getToutesCommandes());
    }

        @GetMapping("/{id}")
    public ResponseEntity<CommandeResponseDto> getCommandeById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getCommandeById(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeResponseDto>> getCommandesParClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(commandeService.getCommandesParClient(clientId));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CommandeResponseDto>> getCommandesParStatut(@PathVariable StatutCommande statut) {
        return ResponseEntity.ok(commandeService.getCommandesParStatut(statut));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<CommandeResponseDto> validerCommande(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.validerCommande(id));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<String> annulerCommande(@PathVariable Long id) {
        commandeService.annulerCommande(id);
        return ResponseEntity.ok("Commande " + id + " annulée avec succès.");
    }

    // --- Endpoints pour les Lignes de Commande ---

    //lezem nahi fazet yejbed des stock men des stock statut ebte33hom expirée
    @PostMapping("/{commandeId}/lignes")
    public ResponseEntity<CommandeResponseDto> ajouterLigneACommande(
            @PathVariable Long commandeId,
            @RequestBody @Valid LigneCommandeRequestDto dto) {
        CommandeResponseDto commandeMiseAJour = ligneCommandeGestionService.ajouterLigneACommande(commandeId, dto);
        return new ResponseEntity<>(commandeMiseAJour, HttpStatus.CREATED);
    }

    //quand je change la quantiter de la ligne il ne enregistre pas en retraitstock cad le stock n'est pas diminer
    @PutMapping("/{commandeId}/lignes/{ligneId}")
    public ResponseEntity<CommandeResponseDto> mettreAJourQuantiteLigne(
            @PathVariable Long commandeId,
            @PathVariable Long ligneId,
            @RequestParam int nouvelleQuantite) {
        CommandeResponseDto commandeMiseAJour = ligneCommandeGestionService.mettreAJourQuantiteLigne(commandeId, ligneId, nouvelleQuantite);
        return ResponseEntity.ok(commandeMiseAJour);
    }

    @DeleteMapping("/{commandeId}/lignes/{ligneId}")
    public ResponseEntity<CommandeResponseDto> supprimerLigneDeCommande(
            @PathVariable Long commandeId,
            @PathVariable Long ligneId) {
        CommandeResponseDto commandeMiseAJour = ligneCommandeGestionService.supprimerLigneDeCommande(commandeId, ligneId);
        return ResponseEntity.ok(commandeMiseAJour);
    }

    // --- Gestion centralisée des erreurs pour ce contrôleur ---
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<String> handleNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequestException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}