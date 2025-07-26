package hackathon.hackathon2025.agents;

public interface StoryPointAgent {

    String communicate(String message);
    String communicateWithExpert(String userMessage, String expertRecommendation);

}
