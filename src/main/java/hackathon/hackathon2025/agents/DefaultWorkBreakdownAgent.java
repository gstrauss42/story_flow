package hackathon.hackathon2025.agents;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;


@Service("workBreakdownAgent")
public class DefaultWorkBreakdownAgent extends DefaultBaseAgent implements WorkBreakdownAgent {

    @Value("${openai.apikey}")
    private String openAiApiKey;

    public DefaultWorkBreakdownAgent() {
        System.out.println("Agent Two initialized.");
    }

    @Override
    public String communicate(String message) {
        ChatModel chatModel = getChatModel();
        WorkBreakdownExpert expertTrendingProducts = AiServices.create(WorkBreakdownExpert.class, chatModel);
        System.out.println("Requesting product expert with message: " + message);
        return expertTrendingProducts.chat(message);
//        return "A mock response from the expert model";
    }



}