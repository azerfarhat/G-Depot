package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.Commande;
import com.GestionDepot.GESTION_DEPOT.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByStatutCommande(StatutCommande statut);

    List<Commande> findByClientId(Long clientId);

    Optional<Commande> findByClientIdAndStatutCommande(Long clientId, StatutCommande statut);

}

