package com.GestionDepot.GESTION_DEPOT.Repository;

import com.GestionDepot.GESTION_DEPOT.Model.Utilisateur;
import com.GestionDepot.GESTION_DEPOT.Response.UtilisateurSimpleDto;
import com.GestionDepot.GESTION_DEPOT.Model.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Long> {

     List<Depot> findByResponsable(Utilisateur responsable);
}
