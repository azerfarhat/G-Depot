package com.GestionDepot.GESTION_DEPOT.Response;

import com.GestionDepot.GESTION_DEPOT.Model.Stock;
import lombok.Getter;

@Getter
public class StockCreationResponse {
    private boolean confirmationRequise;
    private String message;
    private Stock stock;

    public StockCreationResponse(boolean confirmationRequise, String message, Stock stock) {
        this.confirmationRequise = confirmationRequise;
        this.message = message;
        this.stock = stock;
    }

    public void setConfirmationRequise(boolean confirmationRequise) {
        this.confirmationRequise = confirmationRequise;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}