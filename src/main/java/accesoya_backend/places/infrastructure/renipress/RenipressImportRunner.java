package accesoya_backend.places.infrastructure.renipress;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RenipressImportRunner
        implements CommandLineRunner {

    private final RenipressImporter importer;

    @Value("${accesoya.import.renipress.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {

        if (!enabled) {
            return;
        }

        importer.importData();
    }
}