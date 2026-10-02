package com.prototipoBotDiscor.prototipoDiscord.discord;

import net.dv8tion.jda.api.JDA;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DiscordListenerRegistrar implements CommandLineRunner{

    private final JDA jda;
    private final MessageListener messageListener;

    public DiscordListenerRegistrar(JDA jda, MessageListener messageListener) {
        this.jda = jda;
        this.messageListener = messageListener;
    }


    @Override
    public void run(String... args) throws Exception {
        jda.addEventListener(messageListener);
    }
}
