package hackathon.hackathon2025.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


public interface WorkBreakdownExpert {
    @UserMessage("You are a work estimation expert. {{it}}")
    @SystemMessage("You are an expert in breaking down a work statement into what is required to complete the software engineering problem for an agile software engineering team. Each work item should be listed on a new line.")
    String chat(String userMessage);
}
