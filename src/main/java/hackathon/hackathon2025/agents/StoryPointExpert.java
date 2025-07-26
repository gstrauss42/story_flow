package hackathon.hackathon2025.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


public interface StoryPointExpert {
    @UserMessage("You are the software estimation expert. {{it}}")
    @SystemMessage("You are responsible for determining the story points for a list of work items with fibonacci points. do not summarise the work items. add two new lines after each story point for html")
    String chat(String userMessage);
}
