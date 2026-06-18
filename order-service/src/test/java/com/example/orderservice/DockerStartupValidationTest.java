package com.example.orderservice;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DockerStartupValidationTest {

    @Test
    public void testDockerComposeFileExists() {
        File file = new File("../docker-compose.yml");
        if (!file.exists()) {
            file = new File("docker-compose.yml");
        }
        assertTrue(file.exists(), "docker-compose.yml should exist in workspace root directory");
    }
}
