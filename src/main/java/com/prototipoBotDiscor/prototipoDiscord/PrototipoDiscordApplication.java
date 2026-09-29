package com.prototipoBotDiscor.prototipoDiscord;

import com.prototipoBotDiscor.prototipoDiscord.config.DotenvLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
public class PrototipoDiscordApplication {

	public static void main(String[] args) {
		DotenvLoader.carregar();
		SpringApplication.run(PrototipoDiscordApplication.class, args);
	}
}