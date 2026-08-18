package accesoya_backend.places.application.service;

import accesoya_backend.places.application.dto.DashboardDistribution;
import accesoya_backend.places.application.dto.DashboardResponse;
import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.repository.FlmNocDataRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FlmNocDataRepository flmNocDataRepository;

    // =====================================================
    // DASHBOARD
    // =====================================================

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        PlaceStatus status = PlaceStatus.ACTIVE;

        return new DashboardResponse(

                // =================================================
                // TOTAL
                // =================================================

                flmNocDataRepository
                        .countByPlaceStatus(status),

                // =================================================
                // UBICACIÓN
                // =================================================

                map(
                        flmNocDataRepository
                                .countByZonal(status)),

                map(
                        flmNocDataRepository
                                .countByDepartment(status)),

                map(
                        flmNocDataRepository
                                .countByProvince(status)),

                map(
                        flmNocDataRepository
                                .countByDistrict(status)),

                // =================================================
                // TORRE
                // =================================================

                map(
                        flmNocDataRepository
                                .countByTowerOwner(status)),

                map(
                        flmNocDataRepository
                                .countByTowerOwnerClassification(status)),

                // =================================================
                // OPERACIÓN / SEGURIDAD
                // =================================================

                map(
                        flmNocDataRepository
                                .countByReactionCoverage(status)),

                map(
                        flmNocDataRepository
                                .countByPatrol(status)),

                map(
                        flmNocDataRepository
                                .countByGuard(status)),

                map(
                        flmNocDataRepository
                                .countBySurveillance(status)),

                map(
                        flmNocDataRepository
                                .countByDynamicRound(status)),

                map(
                        flmNocDataRepository
                                .countByCsiMonitoring(status)));
    }

    // =====================================================
    // MAPEAR RESULTADOS
    // =====================================================

    private List<DashboardDistribution> map(
            List<Object[]> rows) {

        return rows.stream()

                .map(row -> new DashboardDistribution(

                        row[0] != null
                                ? row[0].toString()
                                : null,

                        ((Number) row[1])
                                .longValue()

                ))

                .toList();
    }
}