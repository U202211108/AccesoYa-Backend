package accesoya_backend.places.application.dto;

import java.util.List;

public record DashboardResponse(

                // =====================================================
                // INFORMACIÓN GENERAL
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

        // =====================================================
        // DASHBOARD CONSULTOR
        // =====================================================

        public static DashboardResponse consultant(

                        long totalSites,

                        List<DashboardDistribution> byDepartment,

                        List<DashboardDistribution> byProvince,

                        List<DashboardDistribution> byDistrict) {

                return new DashboardResponse(

                                totalSites,

                                // Zonal
                                List.of(),

                                // Ubicación general
                                byDepartment,
                                byProvince,
                                byDistrict,

                                // Torre
                                List.of(),
                                List.of(),

                                // Operación / seguridad
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of());
        }

        // =====================================================
        // DASHBOARD OPERADOR FLM/NOC
        // =====================================================

        public static DashboardResponse operator(

                        long totalSites,

                        List<DashboardDistribution> byZonal,

                        List<DashboardDistribution> byDepartment,

                        List<DashboardDistribution> byProvince,

                        List<DashboardDistribution> byDistrict,

                        List<DashboardDistribution> reactionCoverage,

                        List<DashboardDistribution> patrol,

                        List<DashboardDistribution> guard,

                        List<DashboardDistribution> surveillance,

                        List<DashboardDistribution> dynamicRound,

                        List<DashboardDistribution> csiMonitoring) {

                return new DashboardResponse(

                                totalSites,

                                // Ubicación
                                byZonal,
                                byDepartment,
                                byProvince,
                                byDistrict,

                                // Torre
                                List.of(),
                                List.of(),

                                // Operación / seguridad
                                reactionCoverage,
                                patrol,
                                guard,
                                surveillance,
                                dynamicRound,
                                csiMonitoring);
        }

        // =====================================================
        // DASHBOARD SUPERVISOR
        // =====================================================

        public static DashboardResponse supervisor(

                        long totalSites,

                        List<DashboardDistribution> byZonal,

                        List<DashboardDistribution> byDepartment,

                        List<DashboardDistribution> byProvince,

                        List<DashboardDistribution> byDistrict,

                        List<DashboardDistribution> byTowerOwner,

                        List<DashboardDistribution> byTowerOwnerClassification,

                        List<DashboardDistribution> reactionCoverage,

                        List<DashboardDistribution> patrol,

                        List<DashboardDistribution> guard,

                        List<DashboardDistribution> surveillance,

                        List<DashboardDistribution> dynamicRound,

                        List<DashboardDistribution> csiMonitoring) {

                return new DashboardResponse(

                                totalSites,

                                // Ubicación
                                byZonal,
                                byDepartment,
                                byProvince,
                                byDistrict,

                                // Torre
                                byTowerOwner,
                                byTowerOwnerClassification,

                                // Operación / seguridad
                                reactionCoverage,
                                patrol,
                                guard,
                                surveillance,
                                dynamicRound,
                                csiMonitoring);
        }

        // =====================================================
        // DASHBOARD ADMIN
        // =====================================================

        public static DashboardResponse admin(

                        long totalSites,

                        List<DashboardDistribution> byZonal,

                        List<DashboardDistribution> byDepartment,

                        List<DashboardDistribution> byProvince,

                        List<DashboardDistribution> byDistrict,

                        List<DashboardDistribution> byTowerOwner,

                        List<DashboardDistribution> byTowerOwnerClassification,

                        List<DashboardDistribution> reactionCoverage,

                        List<DashboardDistribution> patrol,

                        List<DashboardDistribution> guard,

                        List<DashboardDistribution> surveillance,

                        List<DashboardDistribution> dynamicRound,

                        List<DashboardDistribution> csiMonitoring) {

                return new DashboardResponse(

                                totalSites,

                                byZonal,
                                byDepartment,
                                byProvince,
                                byDistrict,

                                byTowerOwner,
                                byTowerOwnerClassification,

                                reactionCoverage,
                                patrol,
                                guard,
                                surveillance,
                                dynamicRound,
                                csiMonitoring);
        }
}