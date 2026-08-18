package accesoya_backend.places.application.dto;

import java.util.List;

public record DashboardResponse(

        // =====================================================
        // TOTAL
        // =====================================================

        long totalSites,

        // =====================================================
        // UBICACIÓN
        // =====================================================

        List<DashboardDistribution> byZonal,

        List<DashboardDistribution> byDepartment,

        List<DashboardDistribution> byProvince,

        List<DashboardDistribution> byDistrict,

        // =====================================================
        // TORRE
        // =====================================================

        List<DashboardDistribution> byTowerOwner,

        List<DashboardDistribution> byTowerOwnerClassification,

        // =====================================================
        // OPERACIÓN / SEGURIDAD
        // =====================================================

        List<DashboardDistribution> reactionCoverage,

        List<DashboardDistribution> patrol,

        List<DashboardDistribution> guard,

        List<DashboardDistribution> surveillance,

        List<DashboardDistribution> dynamicRound,

        List<DashboardDistribution> csiMonitoring

) {
}