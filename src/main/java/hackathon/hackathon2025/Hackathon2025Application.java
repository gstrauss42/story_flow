package hackathon.hackathon2025;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import hackathon.hackathon2025.agents.DefaultStoryPointAgent;


@SpringBootApplication
public class Hackathon2025Application implements CommandLineRunner {

    @Autowired
    private DefaultStoryPointAgent storyPointAgent;

    public static void main(String[] args) {
        SpringApplication.run(Hackathon2025Application.class, args);

    }

    @Override
    public void run(String... args) {
        System.out.println("Starting agentic AI project...");
    }




}
