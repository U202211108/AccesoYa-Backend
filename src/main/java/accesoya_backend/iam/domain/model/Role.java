package accesoya_backend.iam.domain.model;

public enum Role {

    /**
     * Usuario de consulta.
     * Puede consultar información general
     * de los sitios sin acceder a información
     * operativa FLM/NOC.
     */
    CONSULTOR,

    /**
     * Usuario encargado de operaciones FLM/NOC.
     * Puede consultar información operativa
     * y gestionar planos.
     */
    OPERADOR_FLNOC,

    /**
     * Usuario encargado de supervisar
     * operaciones y sitios FLM/NOC.
     */
    SUPERVISOR,

    /**
     * Administrador general de la plataforma.
     * Tiene control sobre usuarios,
     * roles y configuración operativa.
     */
    ADMIN
}