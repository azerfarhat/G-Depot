package com.GestionDepot.GESTION_DEPOT.Model;

import com.GestionDepot.GESTION_DEPOT.enums.RoleUtilisateur;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String email;
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleUtilisateur role;

    @Size(max = 20)
    private String telephone;

    @Size(max = 30)
    @Column(name = "numero_permis")
    private String numeroPermis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id") // Nom de la colonne de clé étrangère dans la table 'utilisateurs'
    @JsonBackReference("depot-utilisateurs") // Pour gérer la sérialisation bidirectionnelle avec Depot
    private Depot depot;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "vehicule_id", referencedColumnName = "id")
    private Vehicule vehicule; // Relation avec la classe Vehicule

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority(role.name())); }
    @Override public String getPassword() { return motDePasse; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}