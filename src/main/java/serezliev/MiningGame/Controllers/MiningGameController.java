package serezliev.MiningGame.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import serezliev.MiningGame.components.GameParameters;
import serezliev.MiningGame.services.MiningGameService;
import serezliev.MiningGame.services.Worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mining-game")
public class MiningGameController {

    private final MiningGameService miningGameService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MiningGameController(MiningGameService miningGameService, SimpMessagingTemplate messagingTemplate) {
        this.miningGameService = miningGameService;
        this.messagingTemplate = messagingTemplate;
    }


    @GetMapping("/index")
    public String index(Model model) {
        List<Worker> workers = miningGameService.getWorkers();
        model.addAttribute("workers", workers);
        return "index";
    }

    @PostMapping("/workers/add")
    @ResponseBody
    public String addWorker() {
        miningGameService.addWorker();
        broadcastWorkers();
        return "Worker added successfully";
    }

    @DeleteMapping("/workers/remove/{workerId}")
    @ResponseBody
    public String removeWorker(@PathVariable int workerId) {
        miningGameService.removeWorker(workerId);
        broadcastWorkers();
        return "Worker removed successfully";
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(@RequestBody GameParameters gameParams) {
        // Тук GameParameters е POJO клас, който представя JSON данните от заявката
        int initialMineResources = gameParams.getInitialMineResources();
        int initialWorkers = gameParams.getInitialWorkers();

        System.out.println("Received initialMineResources: " + initialMineResources);
        System.out.println("Received initialWorkers: " + initialWorkers);
        // Стартиране на играта с подадените параметри
        miningGameService.startGame(initialMineResources, initialWorkers);

        // Излъчване на информация за миньорите след стартиране на играта
        broadcastWorkers();

        // Получаване на актуална информация за работниците (миньорите)
        List<Worker> workers = miningGameService.getWorkers();

        // Подготвяне на данните за връщане на фронтенда
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "Game started");
        responseData.put("workers", workers);
        responseData.put("status", "success");

        System.out.println(responseData);
        // Връщане на ResponseEntity с данните към фронтенда
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/stop")
    @ResponseBody
    public String stopGame() {
        miningGameService.stopGame();
        broadcastWorkers();
        return "Game stopped";
    }

    private void broadcastWorkers() {
        List<Worker> workers = miningGameService.getWorkers();
        messagingTemplate.convertAndSend("/topic/workers", workers);
    }
}
