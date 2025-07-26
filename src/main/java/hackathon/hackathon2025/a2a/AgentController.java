package hackathon.hackathon2025.a2a;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/input")
    public String handleInput(
            @RequestParam String from,
            @RequestParam String message,
            Model model,
            @SessionAttribute(value = "conversation", required = false) List<Map<String, String>> conversation,
            HttpSession session) {

        if (conversation == null || "clear".equals(message)) {
            conversation = new ArrayList<>();
        }

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