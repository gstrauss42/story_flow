package hackathon.hackathon2025.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


public interface SecurityExpert {
    @UserMessage("You are a security agent. {{it}}")
    @SystemMessage("you are a security expert. determine whether any secrets are being shared in the conversation. If so, answer true, else answer false.")
    String chat(String userMessage);
}
