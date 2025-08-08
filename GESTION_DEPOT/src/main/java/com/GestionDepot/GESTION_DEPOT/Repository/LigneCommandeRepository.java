package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {
}
