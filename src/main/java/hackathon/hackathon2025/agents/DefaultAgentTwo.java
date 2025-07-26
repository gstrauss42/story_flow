package hackathon.hackathon2025.agents;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;


@Service("agentTwo")
public class DefaultAgentTwo extends DefaultBaseAgent implements AgentTwo {

    @Value("${openai.apikey}")
    private String openAiApiKey;

    public DefaultAgentTwo() {
        System.out.println("Agent Two initialized.");
    }

    @Override
    public String communicate(String message) {
        ChatModel chatModel = getChatModel();
        Expert expertTrendingProducts = AiServices.create(Expert.class, chatModel);
        System.out.println("Requesting product expert with message: " + message);
        return expertTrendingProducts.chat(message);
//        return "A mock response from the expert model";
    }



}