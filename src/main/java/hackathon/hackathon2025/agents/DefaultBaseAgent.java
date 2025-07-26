package hackathon.hackathon2025.agents;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public abstract class DefaultBaseAgent {
    private ChatModel chatModel;

    @Value("${openai.apikey}")
    private String openAiApiKey;

    @Value("${spring.application.name}")
    private String appName;

    public DefaultBaseAgent() {

    }

    public void setChatModel() {
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o-mini")
                .build();
    }

    public ChatModel getChatModel() {
        if (chatModel == null) {
            setChatModel();
        }
        return chatModel;
    }
}