package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModerationController {

    public void moderar(MensagemDTO dto){
        log.info("ModerationController recebeu para moderar: {}", dto.getConteudo());
    }

}
