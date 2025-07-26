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

import hackathon.hackathon2025.agents.SecurityAgent;
import hackathon.hackathon2025.agents.StoryPointAgent;
import hackathon.hackathon2025.agents.WorkBreakdownAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/agents")
public class AgentController {

    @Resource(name = "storyPointAgent")
    private StoryPointAgent storyPointAgent;
    
    @Resource(name = "workBreakdownAgent")
    private WorkBreakdownAgent workBreakdownAgent;

    @Resource(name = "securityAgent")
    private SecurityAgent securityAgent;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public AgentController(StoryPointAgent storyPointAgent, WorkBreakdownAgent workBreakdownAgent) {
        this.storyPointAgent = storyPointAgent;
        this.workBreakdownAgent = workBreakdownAgent;
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

        String securityAgentReply = securityAgent.communicate(message);
        boolean securityIssueFound = false;
        try {
            securityIssueFound = Boolean.parseBoolean(securityAgentReply);
        } catch (Exception e) {

        }
        if(securityIssueFound) {
            broadcast("security_expert", Map.of(
                    "processing", Map.of(
                            "header", "security issue found",
                            "content", "Issue #" + issueNumber + "\nParsing: \"" + issueTitle + "\"\nSecurity issue detected!\nPlease review the issue and take necessary actions."
                    )
            ));
            Map<String, Object> response = new HashMap<>();
            return ResponseEntity.ok(response);
        }
        else {
            broadcast("security_expert", Map.of(
                    "processing", Map.of(
                            "header", "no security issues detected ",
                            "content", "Issue #" + issueNumber + "\nParsing: \"" + issueTitle + "\"\nNo security issues detected.\nProceeding with estimation."
                    )
            ));
        }
        
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
        String workBreakdownAgentReply = workBreakdownAgent.communicate(message);
        
        sleep(1500);
        
        // Broadcast: Expert complete
        broadcast("expert_complete", Map.of(
            "message", workBreakdownAgentReply
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
        String storyPointAgentReply = storyPointAgent.communicateWithExpert(message, workBreakdownAgentReply);

        int storyPoints = extractStoryPoints(storyPointAgentReply);
        int confidence = calculateConfidence(storyPointAgentReply);

        sleep(1500);
        
        // Broadcast: Estimation complete
        broadcast("estimation_complete", Map.of(
            "message", storyPointAgentReply,
            "estimation", Map.of(
                "points", storyPoints,
                "confidence", confidence
            )
        ));

        // Return response to GitHub Action
        Map<String, Object> response = new HashMap<>();
        response.put("story_points", storyPoints);
        response.put("explanation", storyPointAgentReply);
        response.put("confidence", confidence / 100.0); // Convert to decimal

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
        if (text == null || text.isEmpty()) {
            return 5; // default
        }
        
        // Map to store all found numbers with their confidence scores
        Map<Integer, Integer> pointCandidates = new HashMap<>();
        
        // Pattern 1: Bold markdown format **X**
        Pattern boldPattern = Pattern.compile("\\*\\*(\\d+)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(text);
        while (boldMatcher.find()) {
            int points = Integer.parseInt(boldMatcher.group(1));
            if (points >= 1 && points <= 100) { // reasonable range
                pointCandidates.put(points, pointCandidates.getOrDefault(points, 0) + 10); // high confidence
            }
        }
        
        // Pattern 2: "story points" followed by various connectors and a number
        Pattern storyPointsPattern = Pattern.compile(
            "story\\s*points?[^\\d]{0,20}(\\d+)|" +  // story points [various text] X
            "(\\d+)\\s*story\\s*points?|" +           // X story points
            "estimate[^\\d]{0,20}(\\d+)|" +           // estimate [various text] X
            "points?[^\\d]{0,10}(\\d+)|" +            // points: X
            "(\\d+)\\s*points?(?:\\s|\\.|,|$)",       // X points
            Pattern.CASE_INSENSITIVE
        );
        Matcher spMatcher = storyPointsPattern.matcher(text);
        while (spMatcher.find()) {
            for (int i = 1; i <= spMatcher.groupCount(); i++) {
                if (spMatcher.group(i) != null) {
                    int points = Integer.parseInt(spMatcher.group(i));
                    if (points >= 1 && points <= 100) {
                        pointCandidates.put(points, pointCandidates.getOrDefault(points, 0) + 8);
                    }
                    break;
                }
            }
        }
        
        // Pattern 3: Common phrases with numbers
        String[] phrases = {
            "estimate.*?(?:is|be|:|=)\\s*(\\d+)",
            "estimated\\s*(?:at|to|:)?\\s*(\\d+)",
            "story\\s*points.*?(?:is|are|be|:|=)\\s*(\\d+)",
            "points.*?(?:is|are|be|:|=)\\s*(\\d+)",
            "complexity.*?(\\d+)",
            "effort.*?(\\d+)",
            "fibonacci.*?(\\d+)",
            "sp[:\\s]*(\\d+)",  // SP: 8 or SP 8
            "^\\s*(\\d+)\\s*$"  // Just a number on its own line
        };
        
        for (String phrase : phrases) {
            Pattern p = Pattern.compile(phrase, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            while (m.find()) {
                int points = Integer.parseInt(m.group(1));
                if (points >= 1 && points <= 100) {
                    pointCandidates.put(points, pointCandidates.getOrDefault(points, 0) + 5);
                }
            }
        }
        
        // Pattern 4: Look for Fibonacci numbers specifically (common in story points)
        int[] fibonacciNumbers = {1, 2, 3, 5, 8, 13, 21, 34, 55, 89};
        Pattern numberPattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher numberMatcher = numberPattern.matcher(text);
        while (numberMatcher.find()) {
            int num = Integer.parseInt(numberMatcher.group(1));
            for (int fib : fibonacciNumbers) {
                if (num == fib) {
                    pointCandidates.put(num, pointCandidates.getOrDefault(num, 0) + 2);
                    break;
                }
            }
        }
        
        // Pattern 5: Look for patterns like "The answer is X" or "Therefore X"
        Pattern conclusionPattern = Pattern.compile(
            "(?:answer|result|therefore|total|overall|final|following).*?(\\d+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher conclusionMatcher = conclusionPattern.matcher(text);
        while (conclusionMatcher.find()) {
            int points = Integer.parseInt(conclusionMatcher.group(1));
            if (points >= 1 && points <= 100) {
                pointCandidates.put(points, pointCandidates.getOrDefault(points, 0) + 7);
            }
        }
        
        // Pattern 6: Check the last few lines (often contains the final answer)
        String[] lines = text.split("\n");
        for (int i = Math.max(0, lines.length - 5); i < lines.length; i++) {
            Matcher lineMatcher = numberPattern.matcher(lines[i]);
            while (lineMatcher.find()) {
                int num = Integer.parseInt(lineMatcher.group(1));
                if (num >= 1 && num <= 100) {
                    // Higher confidence for numbers in the last lines
                    pointCandidates.put(num, pointCandidates.getOrDefault(num, 0) + 3);
                }
            }
        }
        
        // Find the number with the highest confidence score
        if (!pointCandidates.isEmpty()) {
            return pointCandidates.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(5);
        }
        
        // Last resort: find any number between 1-100
        Matcher anyNumber = Pattern.compile("\\b(\\d{1,2})\\b").matcher(text);
        while (anyNumber.find()) {
            int num = Integer.parseInt(anyNumber.group(1));
            if (num >= 1 && num <= 40) { // reasonable story point range
                return num;
            }
        }
        
        return 5; // default if nothing found
    }
    
    private int calculateConfidence(String text) {
        String lowerText = text.toLowerCase();
        int confidence = 70; // base confidence
        
        // Positive confidence indicators
        if (lowerText.contains("clear") || lowerText.contains("straightforward") || 
            lowerText.contains("well-defined") || lowerText.contains("simple")) {
            confidence += 15;
        }
        if (lowerText.contains("similar") || lowerText.contains("typical") || 
            lowerText.contains("standard") || lowerText.contains("common")) {
            confidence += 10;
        }
        if (lowerText.contains("confident") || lowerText.contains("certain")) {
            confidence += 10;
        }
        
        // Negative confidence indicators
        if (lowerText.contains("complex") || lowerText.contains("uncertain") || 
            lowerText.contains("unclear") || lowerText.contains("ambiguous")) {
            confidence -= 15;
        }
        if (lowerText.contains("risk") || lowerText.contains("unknown") || 
            lowerText.contains("assumption")) {
            confidence -= 10;
        }
        if (lowerText.contains("lack of clarity") || lowerText.contains("not clear") ||
            lowerText.contains("further clarification")) {
            confidence -= 10;
        }
        
        // Technical complexity indicators
        if (lowerText.contains("multiple components") || lowerText.contains("various components") ||
            lowerText.contains("several systems")) {
            confidence -= 5;
        }
        
        return Math.max(40, Math.min(95, confidence));
    }

    @PostMapping("/test")
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
                String expertReply = workBreakdownAgent.communicate(message);
                String estimatorReply = storyPointAgent.communicateWithExpert(message, expertReply);
                agentMsg.put("from", "estimator");
                agentMsg.put("text", estimatorReply);
            } else {
                String expertReply = workBreakdownAgent.communicate(message);
                agentMsg.put("from", "expert");
                agentMsg.put("text", expertReply);
            }
            conversation.add(agentMsg);

            // Store conversation in session
            session.setAttribute("conversation", conversation);
            model.addAttribute("conversation", conversation);
        }

        return "test";
    }

    @GetMapping("/a2a")
    public String agentToAgent(@RequestParam String from, @RequestParam String message) {
        if ("one".equalsIgnoreCase(from)) {
            return workBreakdownAgent.communicate(message);
        } else {
            return storyPointAgent.communicate(message);
        }
    }
}