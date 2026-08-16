package accesoya_backend.places.infrastructure.openstreetmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenStreetMapImportRunner
        implements CommandLineRunner {

    private final OpenStreetMapImporter importer;

    @Value("${accesoya.import.openstreetmap.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {

        if (!enabled) {

            log.info(
                    "Importación OpenStreetMap deshabilitada.");

            return;
        }

        try {

            importer.importData();

        } catch (Exception exception) {

            log.error(
                    "No se pudo completar la importación OpenStreetMap. "
                            + "El backend continuará ejecutándose.",
                    exception);
        }
    }
}