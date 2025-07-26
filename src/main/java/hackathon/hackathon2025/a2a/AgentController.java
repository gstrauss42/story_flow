package hackathon.hackathon2025.a2a;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import hackathon.hackathon2025.agents.AgentOne;
import hackathon.hackathon2025.agents.AgentTwo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/agents")
public class AgentController {

    @Resource(name = "agentOne")
    private AgentOne agentOne;
    @Resource(name = "agentTwo")
    private AgentTwo agentTwo;

    public AgentController(AgentOne agentOne, AgentTwo agentTwo) {
        this.agentOne = agentOne;
        this.agentTwo = agentTwo;
    }

    @GetMapping("/input")
    public String showInputPage() {
        return "input";
    }

    @PostMapping("/estimate")
    public ResponseEntity<Map<String, Object>> estimateStoryPoints(
            @RequestBody Map<String, String> request) {

        String message = request.get("message");

        // Get estimation from your agents
        String expertReply = agentTwo.communicate(message);
        String estimatorReply = agentOne.communicateWithExpert(message, expertReply);

        // Parse story points from response
        int storyPoints = extractStoryPoints(estimatorReply);

        Map<String, Object> response = new HashMap<>();
        response.put("story_points", storyPoints);
        response.put("explanation", estimatorReply);
        response.put("confidence", 0.8);

        return ResponseEntity.ok(response);
    }

    private int extractStoryPoints(String text) {
        // Extract number from text
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:story\\s*)?points?",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 5; // default
    }

    @PostMapping("/input")
    public String handleInput(
            @RequestParam String from,
            @RequestParam String message,
            Model model,
            @SessionAttribute(value = "conversation", required = false) List<Map<String, String>> conversation,
            HttpSession session) {

        if (conversation == null ) {
            conversation = new ArrayList<>();
        }

        if("clear".equals(message)) {
            conversation = new ArrayList<>();
            session.setAttribute("conversation", conversation);
        } else {
            // Add user message
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("from", from);
            userMsg.put("text", message);
            conversation.add(userMsg);

            Map<String, String> agentMsg = new HashMap<>();
            if ("estimator".equalsIgnoreCase(from)) {
                String expertReply = agentTwo.communicate(message);
                String estimatorReply = agentOne.communicateWithExpert(message, expertReply);
                agentMsg.put("from", "estimator");
                agentMsg.put("text", estimatorReply);
            } else {
                String expertReply = agentTwo.communicate(message);
                agentMsg.put("from", "expert");
                agentMsg.put("text", expertReply);
            }
            conversation.add(agentMsg);

            // Store conversation in session
            session.setAttribute("conversation", conversation);

            model.addAttribute("conversation", conversation);

        }


        return "input";
    }


    @GetMapping("/a2a")
    public String agentToAgent(@RequestParam String from, @RequestParam String message) {
        if ("one".equalsIgnoreCase(from)) {
            return agentTwo.communicate(message);
        } else {
            return agentOne.communicate(message);
        }
    }
}