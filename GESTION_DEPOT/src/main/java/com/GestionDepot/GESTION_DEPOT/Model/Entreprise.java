//package com.GestionDepot.GESTION_DEPOT.Model;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "entreprises")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Entreprise {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotBlank(message = "Le nom de l'entreprise est obligatoire.")
//    @Size(max = 100)
//    @Column(nullable = false, unique = true)
//    private String nom;
//
//    @Column(columnDefinition = "TEXT")
//    private String adresse;
//
//    @Size(max = 20)
//    private String telephone;
//
//    @Email(message = "L'adresse email doit être valide.")
//    @Size(max = 100)
//    private String email;
//
//    @Size(max = 50)
//    private String matriculeFiscal;
//
//    // === NOUVEAU CHAMP AJOUTÉ ===
//    @Size(max = 24, message = "Le RIB doit contenir 24 caractères en Tunisie.") // Ajustez la taille si nécessaire
//    @Column(name = "rib", length = 24)
//    private String rib;
//}