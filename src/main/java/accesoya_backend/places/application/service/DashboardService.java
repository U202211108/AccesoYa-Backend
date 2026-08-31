package accesoya_backend.places.application.service;

import accesoya_backend.iam.domain.model.Role;
import accesoya_backend.iam.domain.model.User;

import accesoya_backend.places.application.dto.DashboardDistribution;
import accesoya_backend.places.application.dto.DashboardResponse;

import accesoya_backend.places.domain.model.PlaceStatus;
import accesoya_backend.places.domain.repository.FlmNocDataRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final FlmNocDataRepository flmNocDataRepository;

        // =====================================================
        // DASHBOARD SEGÚN ROL
        // =====================================================

        @Transactional(readOnly = true)
        public DashboardResponse getDashboard(
                        Authentication authentication) {

                Role role = getRole(authentication);

                PlaceStatus status = PlaceStatus.ACTIVE;

                // =====================================================
                // INFORMACIÓN GENERAL
                // =====================================================

                long totalSites = flmNocDataRepository
                                .countByPlaceStatus(status);

                List<DashboardDistribution> byDepartment = map(
                                flmNocDataRepository
                                                .countByDepartment(status));

                List<DashboardDistribution> byProvince = map(
                                flmNocDataRepository
                                                .countByProvince(status));

                List<DashboardDistribution> byDistrict = map(
                                flmNocDataRepository
                                                .countByDistrict(status));

                // =====================================================
                // CONSULTOR
                // =====================================================

                if (role == Role.CONSULTOR) {

                        return DashboardResponse.consultant(

                                        totalSites,

                                        byDepartment,
                                        byProvince,
                                        byDistrict);
                }

                // =====================================================
                // INFORMACIÓN ZONAL
                // =====================================================

                List<DashboardDistribution> byZonal = map(
                                flmNocDataRepository
                                                .countByZonal(status));

                // =====================================================
                // OPERACIÓN / SEGURIDAD
                // =====================================================

                List<DashboardDistribution> reactionCoverage = map(
                                flmNocDataRepository
                                                .countByReactionCoverage(status));

                List<DashboardDistribution> patrol = map(
                                flmNocDataRepository
                                                .countByPatrol(status));

                List<DashboardDistribution> guard = map(
                                flmNocDataRepository
                                                .countByGuard(status));

                List<DashboardDistribution> surveillance = map(
                                flmNocDataRepository
                                                .countBySurveillance(status));

                List<DashboardDistribution> dynamicRound = map(
                                flmNocDataRepository
                                                .countByDynamicRound(status));

                List<DashboardDistribution> csiMonitoring = map(
                                flmNocDataRepository
                                                .countByCsiMonitoring(status));

                // =====================================================
                // OPERADOR FLM/NOC
                // =====================================================

                if (role == Role.OPERADOR_FLNOC) {

                        return DashboardResponse.operator(

                                        totalSites,

                                        byZonal,

                                        byDepartment,
                                        byProvince,
                                        byDistrict,

                                        reactionCoverage,
                                        patrol,
                                        guard,
                                        surveillance,
                                        dynamicRound,
                                        csiMonitoring);
                }

                // =====================================================
                // INFORMACIÓN DE TORRES
                // =====================================================

                List<DashboardDistribution> byTowerOwner = map(
                                flmNocDataRepository
                                                .countByTowerOwner(status));

                List<DashboardDistribution> byTowerOwnerClassification = map(
                                flmNocDataRepository
                                                .countByTowerOwnerClassification(status));

                // =====================================================
                // SUPERVISOR
                // =====================================================

                if (role == Role.SUPERVISOR) {

                        return DashboardResponse.supervisor(

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

                // =====================================================
                // ADMIN
                // =====================================================

                if (role == Role.ADMIN) {

                        return DashboardResponse.admin(

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

                // =====================================================
                // ROL NO VÁLIDO
                // =====================================================

                throw new SecurityException(
                                "El usuario no tiene un rol válido para acceder al dashboard");
        }

        // =====================================================
        // OBTENER ROL
        // =====================================================

        private Role getRole(
                        Authentication authentication) {

                if (authentication == null ||
                                !(authentication.getPrincipal() instanceof User user)) {

                        throw new SecurityException(
                                        "No se pudo identificar al usuario autenticado");
                }

                if (user.getRole() == null) {

                        throw new SecurityException(
                                        "El usuario autenticado no tiene un rol asignado");
                }

                return user.getRole();
        }

        // =====================================================
        // MAPEAR RESULTADOS
        // =====================================================

        private List<DashboardDistribution> map(
                        List<Object[]> rows) {

                if (rows == null ||
                                rows.isEmpty()) {

                        return List.of();
                }

                return rows.stream()

                                .map(row -> new DashboardDistribution(

                                                row[0] != null
                                                                ? row[0].toString()
                                                                : null,

                                                row[1] != null
                                                                ? ((Number) row[1])
                                                                                .longValue()
                                                                : 0L))

                                .toList();
        }
}