package com.GestionDepot.GESTION_DEPOT.Controller;

import com.GestionDepot.GESTION_DEPOT.Model.Facture;
import com.GestionDepot.GESTION_DEPOT.Request.FactureCreateRequestDto;
import com.GestionDepot.GESTION_DEPOT.Request.FactureStatutUpdateDto;
import com.GestionDepot.GESTION_DEPOT.Service.FactureService;
import com.GestionDepot.GESTION_DEPOT.Response.FactureResponseDto;
import com.GestionDepot.GESTION_DEPOT.dto.FactureCreateDTO;
import com.GestionDepot.GESTION_DEPOT.dto.FactureDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin(origins = "*")
public class FactureController {

    private final FactureService factureService;

    public FactureController(FactureService factureService) {
        this.factureService = factureService;
    }

    @PostMapping("/chauffeur")
    public ResponseEntity<FactureDTO> creerFactureChauffeur(@Valid @RequestBody FactureCreateDTO dto) {
        FactureDTO nouvelleFacture = factureService.creerFacturePourChauffeur(dto);
        return ResponseEntity.ok(nouvelleFacture);
    }

    @PostMapping(
            path = "/commande/{commandeId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> creerFacture(
            @PathVariable Long commandeId,
            @Valid @RequestBody FactureCreateRequestDto requestDto) {
        try {
            FactureResponseDto nouvelleFacture = factureService.creerFacturePourCommande(commandeId, requestDto);
            return new ResponseEntity<>(nouvelleFacture, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //changement statut du facture
    @PatchMapping(
            path = "/{id}/statut",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateFactureStatut(
            @PathVariable("id") Long factureId,
            @Valid @RequestBody FactureStatutUpdateDto dto) {
        try {
            FactureResponseDto factureMiseAJour = factureService.updateStatutFacture(factureId, dto.getNouveauStatut());
            return ResponseEntity.ok(factureMiseAJour);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Récupère toutes les factures du système.
     */
    @GetMapping
    public ResponseEntity<List<FactureResponseDto>> getToutesFactures() {
        return ResponseEntity.ok(factureService.getToutesFactures());
    }

    /**
     * Récupère une facture par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFactureById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(factureService.getFactureById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Récupère une facture par l'ID de la commande associée.
     */
    @GetMapping("/commande/{commandeId}") // <--- NOUVEL ENDPOINT
    public ResponseEntity<?> getFactureByCommandeId(@PathVariable Long commandeId) {
        try {
            return ResponseEntity.ok(factureService.getFactureByCommandeId(commandeId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Récupère une facture par l'ID du bon de sortie associé.
     */
    @GetMapping("/bon-de-sortie/{bonDeSortieId}") // <--- NOUVEL ENDPOINT
    public ResponseEntity<?> getFactureByBonDeSortieId(@PathVariable Long bonDeSortieId) {
        try {
            return ResponseEntity.ok(factureService.getFactureByBonDeSortieId(bonDeSortieId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Récupère toutes les factures pour un client donné.
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FactureResponseDto>> getFacturesParClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(factureService.getFacturesParClientId(clientId));
    }

    /**
     * Récupère toutes les factures pour un chauffeur donné.
     */
    @GetMapping("/chauffeur/{chauffeurId}") // <--- NOUVEL ENDPOINT
    public ResponseEntity<List<FactureResponseDto>> getFacturesByChauffeurId(@PathVariable Long chauffeurId) {
        return ResponseEntity.ok(factureService.getFacturesByChauffeurId(chauffeurId));
    }
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> telechargerFacturePdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = factureService.generateFacturePdf(id);

            HttpHeaders headers = new HttpHeaders();
            // Définit le type de contenu comme PDF
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Indique au navigateur de télécharger le fichier avec un nom spécifique
            headers.setContentDispositionFormData("attachment", "facture_" + id + ".pdf");
            // Définit la taille du fichier
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            // Si la facture n'est pas trouvée
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage().getBytes());
        } catch (IOException e) {
            // Gérer les erreurs de génération de PDF
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Erreur lors de la génération du PDF: " + e.getMessage()).getBytes());
        }
    }
}