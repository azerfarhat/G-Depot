package com.GestionDepot.GESTION_DEPOT.Service;

import com.GestionDepot.GESTION_DEPOT.Repository.*;
import com.GestionDepot.GESTION_DEPOT.dto.*;
import com.GestionDepot.GESTION_DEPOT.enums.StatutFacture;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FactureRepository factureRepository;
    private final RetraitStockRepository retraitStockRepository;
    private final FournisseurRepository fournisseurRepository;


    public DashboardStatsDto getDashboardStats() {
        LocalDate debutMoisActuel = LocalDate.now().withDayOfMonth(1);
        LocalDate finMoisActuel = YearMonth.now().atEndOfMonth();

        LocalDate debutMoisPrecedent = debutMoisActuel.minusMonths(1);
        LocalDate finMoisPrecedent = debutMoisActuel.minusDays(1);

        long entreesMoisActuel = stockRepository.countMonthlyEntries(debutMoisActuel, finMoisActuel);
        long sortiesMoisActuel = retraitStockRepository.countMonthlyExits(debutMoisActuel.atStartOfDay(), finMoisActuel.atTime(23, 59, 59));
        BigDecimal revenuMoisActuel = factureRepository.findMonthlyRevenue(StatutFacture.PAYEE, debutMoisActuel).orElse(BigDecimal.ZERO);

        long entreesMoisPrecedent = stockRepository.countMonthlyEntries(debutMoisPrecedent, finMoisPrecedent);
        long sortiesMoisPrecedent = retraitStockRepository.countMonthlyExits(debutMoisPrecedent.atStartOfDay(), finMoisPrecedent.atTime(23, 59, 59));
        BigDecimal revenuMoisPrecedent = factureRepository.findMonthlyRevenue(StatutFacture.PAYEE, debutMoisPrecedent).orElse(BigDecimal.ZERO);

        DashboardStatsDto statsDto = new DashboardStatsDto(
                produitRepository.count(),
                entreesMoisActuel,
                sortiesMoisActuel,
                stockRepository.countActiveAlerts(),
                revenuMoisActuel,
                commandeRepository.count(),
                utilisateurRepository.count()
        );

        statsDto.setChangeInEntries(calculatePercentageChange(entreesMoisPrecedent, entreesMoisActuel));
        statsDto.setChangeInExits(calculatePercentageChange(sortiesMoisPrecedent, sortiesMoisActuel));
        statsDto.setChangeInRevenue(calculatePercentageChange(revenuMoisPrecedent, revenuMoisActuel));

        return statsDto;
    }

    private Double calculatePercentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return (newValue > 0) ? 100.0 : 0.0;
        }
        return ((newValue - oldValue) / oldValue) * 100.0;
    }

    private Double calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.equals(BigDecimal.ZERO)) {
            return (newValue.compareTo(BigDecimal.ZERO) > 0) ? 100.0 : 0.0;
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public List<TopProductDto> getTopProducts() {
        return produitRepository.findTopActiveProducts(PageRequest.of(0, 5));
    }

    public List<TopSupplierDto> getTopSuppliersByProductCount() {
        Pageable topFive = PageRequest.of(0, 5);
        return fournisseurRepository.findTopSuppliersByProductCount(topFive);
    }

    public List<AlertDto> getRecentAlerts() {
        return stockRepository.findRecentAlerts();
    }



    public List<StockMovementDto> getStockMovements() {
        LocalDate startDate = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        List<Object[]> entryResults = stockRepository.findMonthlyEntries(startDate);
        Map<String, Long> entriesMap = entryResults.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        List<Object[]> exitResults = retraitStockRepository.findMonthlyExits(startDate.atStartOfDay());
        Map<String, Long> exitsMap = exitResults.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        List<StockMovementDto> result = new ArrayList<>();
        DateTimeFormatter monthLabelFormatter = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH);
        DateTimeFormatter monthKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 5; i >= 0; i--) {
            LocalDate currentDate = LocalDate.now().minusMonths(i);
            String monthKey = currentDate.format(monthKeyFormatter);
            String monthLabel = currentDate.format(monthLabelFormatter);

            long entries = entriesMap.getOrDefault(monthKey, 0L);
            long exits = exitsMap.getOrDefault(monthKey, 0L);

            result.add(new StockMovementDto(monthLabel, entries, exits));
        }
        return result;
    }
}