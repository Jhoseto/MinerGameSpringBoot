package serezliev.MiningGame.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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


    @Autowired
    public MiningGameController(MiningGameService miningGameService) {
        this.miningGameService = miningGameService;
    }

    @PostMapping("/workers/add")
    @ResponseBody
    public ResponseEntity<String> addWorker() {
        miningGameService.addWorker();
        miningGameService.broadcastWorkers();
        return ResponseEntity.ok("Worker added successfully");
    }

    @PostMapping("/workers/remove/{workerId}")
    @ResponseBody
    public ResponseEntity<String> removeWorker(@PathVariable int workerId) {
        miningGameService.removeWorker(workerId);
        miningGameService.broadcastWorkers();
        return ResponseEntity.ok("Worker removed successfully");
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(@RequestBody GameParameters gameParams) {
        if (miningGameService.isPaused()){
            miningGameService.resumeGame();

        }
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

    @PostMapping("/restart")
    public ResponseEntity<String> restartGame() {
        try {
            // Извикване на метода за рестартиране на играта от сървиса
            miningGameService.stopGame();
            miningGameService.restartGame();
            return ResponseEntity.ok("Game restarted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to restart game.");
        }
    }

}
