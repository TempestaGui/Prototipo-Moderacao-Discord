package com.prototipoBotDiscor.prototipoDiscord.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class DotenvLoader {

    private DotenvLoader(){}

    public static void carregar(){
        try{
            Path envFile = Path.of(".env");
            if(!Files.exists(envFile)){
                log.warn(".env não encontrado em {}", envFile.toAbsolutePath());
                return;
            }
            List<String> lines = Files.readAllLines(envFile);
            for(String line: lines){
                line = line.trim();
                if(line.isEmpty() || line.startsWith("#") || !line.contains("=")){
                    continue;
                }
                int idx = line.indexOf('=');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if(System.getenv(key) == null){
                    System.setProperty(key, value);
                }
            }


        }catch(IOException e){
            throw new RuntimeException("Falha ao carregar .env", e);
        }
    }
}

