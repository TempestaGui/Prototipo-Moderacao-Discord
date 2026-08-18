package com.prototipoBotDiscor.prototipoDiscord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@SpringBootApplication
public class PrototipoDiscordApplication {

	public static void main(String[] args) throws IOException {
		loadDotenv();
		SpringApplication.run(PrototipoDiscordApplication.class, args);
	}

	private static void loadDotenv() throws IOException {
		Path envFile = Path.of(".env");
		if (!Files.exists(envFile)) {
			log.warn(".env não encontrado em {}", envFile.toAbsolutePath());
			return;
		}
		List<String> lines = Files.readAllLines(envFile);
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
				continue;
			}
			int idx = line.indexOf('=');
			String key = line.substring(0, idx).trim();
			String value = line.substring(idx + 1).trim();
			if (System.getenv(key) == null) {
				System.setProperty(key, value);
			}
		}
	}
}