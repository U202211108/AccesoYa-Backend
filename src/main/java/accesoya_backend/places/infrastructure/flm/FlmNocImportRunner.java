package accesoya_backend.places.infrastructure.flm;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlmNocImportRunner
        implements CommandLineRunner {

    private final FlmNocImporter importer;

    @Value("${accesoya.import.flm-noc.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {

        if (!enabled) {
            return;
        }

        importer.importData();
    }
}