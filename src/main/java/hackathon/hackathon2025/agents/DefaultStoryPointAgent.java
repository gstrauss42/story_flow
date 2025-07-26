package hackathon.hackathon2025.agents;

import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;


@Service("storyPointAgent")
public class DefaultStoryPointAgent extends DefaultBaseAgent implements StoryPointAgent {

    public DefaultStoryPointAgent() {
        System.out.println("Agent One initialized.");
    }

    @Override
    public String communicate(String message) {
        ChatModel chatModel = getChatModel();
        StoryPointExpert storyPointExpert = AiServices.create(StoryPointExpert.class, chatModel);
        System.out.println("Requesting friend model with message: " + message);
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