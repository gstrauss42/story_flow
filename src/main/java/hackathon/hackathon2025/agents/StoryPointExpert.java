package hackathon.hackathon2025.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


public interface StoryPointExpert {
    @UserMessage("You are the software estimation expert. {{it}}")
    @SystemMessage("You are responsible for determining the story points for a list of work items with fibonacci points. after each work item create a new line for html.")
    String chat(String userMessage);
}
