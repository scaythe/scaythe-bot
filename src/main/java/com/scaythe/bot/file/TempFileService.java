package com.scaythe.bot.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TempFileService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Collection<Path> registry = new ArrayList<>();

    public Path newFile(byte[] content) throws IOException {
        Path path = Files.createTempFile(null, null);
        log.debug("writing to {}", path);

        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(content);
        }

        registry.add(path);

        return path;
    }

    public void deleteFile(Path path) throws IOException {
        log.debug("deleting {}", path);
//        Files.delete(path);

        registry.remove(path);
    }

    @PreDestroy
    public void clean() {
        log.debug("cleaning");
        if (!registry.isEmpty()) {
            log.debug("cleaning remaining files");

            for (Path path : registry) {
                log.debug("deleting {}", path);
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.error("error cleaning {} : {}", path, e.getMessage());
                    log.trace("", e);
                }
            }
        }
    }
}
