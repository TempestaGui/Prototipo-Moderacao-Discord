package com.prototipoBotDiscor.prototipoDiscord.moderation;

import com.prototipoBotDiscor.prototipoDiscord.dto.MensagemDTO;
import com.prototipoBotDiscor.prototipoDiscord.gemini.GeminiService;
import com.prototipoBotDiscor.prototipoDiscord.gemini.RespostaGeminiDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModerationControllerTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private ModerationController moderationController;

    @Test
    void deveChamarGeminiServiceComDTOCorreto(){
        //Arrange
        MensagemDTO dto = new MensagemDTO(
                "AutorTeste", "AutorTeste",
                "CanalTest", "CanalTeste",
                "MensagemTeste", "MensagemTeste", Instant.now());

        RespostaGeminiDTO respostaGeminiDTO = new RespostaGeminiDTO("SEGURO", "teste", 1.0);
        when(geminiService.analisar(dto)).thenReturn(respostaGeminiDTO);

        //act
        moderationController.moderar(dto);

        //then
        verify(geminiService).analisar(dto);
    }
}
