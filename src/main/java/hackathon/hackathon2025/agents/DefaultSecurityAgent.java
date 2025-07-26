package hackathon.hackathon2025.agents;

import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;


@Service("securityAgent")
public class DefaultSecurityAgent extends DefaultBaseAgent implements AgentOne{

    public DefaultSecurityAgent() {
        System.out.println("Security agent initialized.");
    }

    @Override
    public String communicate(String message) {
        ChatModel chatModel = getChatModel();
        StoryPointExpert storyPointExpert = AiServices.create(StoryPointExpert.class, chatModel);
        System.out.println("Requesting security model with message: " + message);
        return storyPointExpert.chat(message);
//        return "A mock response from the friend model";

    }

    @Override
    public String communicateWithExpert(String userMessage, String expertRecommendation){
        ChatModel chatModel = getChatModel();
        String combinedMessage = "Expert recommends: " + expertRecommendation + "\nUser asked: " + userMessage;
        System.out.println(combinedMessage);

        StoryPointExpert storyPointExpert = AiServices.create(StoryPointExpert.class, chatModel);
        return storyPointExpert.chat(combinedMessage);

    }


}