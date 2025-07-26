package hackathon.hackathon2025.agents;

import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;


@Service("securityAgent")
public class DefaultSecurityAgent extends DefaultBaseAgent implements SecurityAgent {

    public DefaultSecurityAgent() {
        System.out.println("Security agent initialized.");
    }

    @Override
    public String communicate(String message) {
        ChatModel chatModel = getChatModel();
        SecurityExpert securityExpert = AiServices.create(SecurityExpert.class, chatModel);
        System.out.println("Requesting security model with message: " + message);
        return securityExpert.chat(message);
//        return "A mock response from the friend model";

    }



}