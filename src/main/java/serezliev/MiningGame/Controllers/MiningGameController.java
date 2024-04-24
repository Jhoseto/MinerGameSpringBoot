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


    @PostMapping("/workers/add")
    @ResponseBody
    public ResponseEntity<String> addWorker() {
        miningGameService.addWorker();
        miningGameService.broadcastWorkers();
        return ResponseEntity.ok("Worker added successfully");
    }

    @DeleteMapping("/workers/remove/{workerId}")
    @ResponseBody
    public ResponseEntity<String> removeWorker(@PathVariable int workerId) {
        miningGameService.removeWorker(workerId);
        miningGameService.broadcastWorkers();
        return ResponseEntity.ok("Worker removed successfully");
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(@RequestBody GameParameters gameParams) {
        int initialMineResources = gameParams.getInitialMineResources();
        int initialWorkers = gameParams.getInitialWorkers();

        miningGameService.startGame(initialMineResources, initialWorkers);
        miningGameService.broadcastWorkers();

        List<Worker> workers = miningGameService.getWorkers();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "Game started");
        responseData.put("workers", workers);
        responseData.put("data", "success");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopGame() {
        miningGameService.stopGame();
        miningGameService.broadcastWorkers();
        return ResponseEntity.ok("Game stopped");
    }

}
