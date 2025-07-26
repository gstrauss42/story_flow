package hackathon.hackathon2025.a2a;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
        
        // Extract issue information from the GitHub message
        String issueTitle = extractIssueTitle(message);
        String issueNumber = extractIssueNumber(message);
        
        // Broadcast: Issue received
        broadcast("issue_received", Map.of(
            "issueTitle", issueTitle,
            "issueNumber", issueNumber
        ));
        
        // Small delay for UI effect
        sleep(500);
        
        // Broadcast: Expert thinking
        broadcast("expert_thinking", null);
        
        sleep(1000);
        
        // Broadcast: Expert processing
        broadcast("expert_processing", Map.of(
            "processing", Map.of(
                "header", "Analyzing Issue Complexity",
                "content", "Issue #" + issueNumber + "\nParsing: \"" + issueTitle + "\"\nIdentifying key components...\nChecking for dependencies...\nEvaluating technical complexity..."
            )
        ));
        
        // Get expert analysis
        String expertReply = agentTwo.communicate(message);
        
        sleep(1500);
        
        // Broadcast: Expert complete
        broadcast("expert_complete", Map.of(
            "message", expertReply
        ));
        
        sleep(500);
        
        // Broadcast: Estimator thinking
        broadcast("estimator_thinking", null);
        
        sleep(1000);
        
        // Broadcast: Estimator processing
        broadcast("estimator_processing", Map.of(
            "processing", Map.of(
                "header", "Calculating Story Points",
                "content", "Expert input received\nAnalyzing complexity factors...\nEvaluating implementation effort...\nConsidering testing requirements...\nApplying Fibonacci sequence..."
            )
        ));
        
        // Get estimation
        String estimatorReply = agentOne.communicateWithExpert(message, expertReply);
        int storyPoints = extractStoryPoints(estimatorReply);
        
        sleep(1500);
        
        // Broadcast: Estimation complete
        broadcast("estimation_complete", Map.of(
            "message", estimatorReply,
            "estimation", Map.of(
                "points", storyPoints,
                "confidence", calculateConfidence(estimatorReply)
            )
        ));

        // Return response to GitHub Action
        Map<String, Object> response = new HashMap<>();
        response.put("story_points", storyPoints);
        response.put("explanation", estimatorReply);
        response.put("confidence", 0.8);

        return ResponseEntity.ok(response);
    }
    
    private void broadcast(String type, Map<String, Object> data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        if (data != null) {
            message.putAll(data);
        }
        messagingTemplate.convertAndSend("/topic/estimation", message);
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private String extractIssueTitle(String message) {
        Pattern pattern = Pattern.compile("Title:\\s*(.+?)(?:\\n|$)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Unknown Issue";
    }
    
    private String extractIssueNumber(String message) {
        Pattern pattern = Pattern.compile("Issue\\s*#(\\d+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }

    private int extractStoryPoints(String text) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:story\\s*)?points?",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 5; // default
    }
    
    private int calculateConfidence(String text) {
        // Simple confidence calculation based on keywords
        String lowerText = text.toLowerCase();
        int confidence = 70; // base confidence
        
        if (lowerText.contains("clear") || lowerText.contains("straightforward")) {
            confidence += 15;
        }
        if (lowerText.contains("complex") || lowerText.contains("uncertain")) {
            confidence -= 15;
        }
        if (lowerText.contains("similar") || lowerText.contains("typical")) {
            confidence += 10;
        }
        
        return Math.max(50, Math.min(95, confidence));
    }

    @PostMapping("/input")
    public String handleInput(
            @RequestParam String from,
            @RequestParam String message,
            Model model,
            @SessionAttribute(value = "conversation", required = false) List<Map<String, String>> conversation,
            HttpSession session) {

        if (conversation == null) {
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