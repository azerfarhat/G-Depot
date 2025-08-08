// --- COPY AND PASTE THIS ENTIRE FILE ---

package com.GestionDepot.GESTION_DEPOT.dto;

import com.GestionDepot.GESTION_DEPOT.enums.StatutStock;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor // Keep the no-args constructor
public class AlertDto {

    private String productName;
    private String message;
    private StatutStock type;
    private LocalDateTime createdAt;

    /**
     * This explicit public constructor is what JPQL's `SELECT new...` will use.
     * The order and types of the parameters MUST EXACTLY match the SELECT clause.
     * 1. String (from p.nom)
     * 2. String (from CONCAT)
     * 3. StatutStock (from CASE WHEN)
     * 4. LocalDateTime (from MAX(s_date.createdAt))
     */
    public AlertDto(String productName, String message, StatutStock type, LocalDateTime createdAt) {
        this.productName = productName;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
    }
}