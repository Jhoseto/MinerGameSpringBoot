package serezliev.MiningGame.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import serezliev.MiningGame.services.MiningGameService;
import serezliev.MiningGame.services.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MiningGameServiceImpl implements MiningGameService {

    private static final int MAX_MINE_SIZE = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_MINE_SIZE);
    private int totalResourcesInMine = 0;
    private final List<Worker> workers = new ArrayList<>();
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MiningGameServiceImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public List<Worker> getWorkers() {
        return workers;
    }

    @Override
    public void addWorker() {
        if (workers.size() < MAX_MINE_SIZE) {
            Worker newWorker = new WorkerImpl(this,messagingTemplate);
            workers.add(newWorker);
            executor.submit((Runnable) newWorker);
        }
    }

    @Override
    public void removeWorker(int workerId) {
        // Намиране на работника по даден workerId
        Optional<Worker> workerToRemove = workers.stream()
                .filter(worker -> worker.getId() == workerId)
                .findFirst();

        if (workerToRemove.isPresent()) {
            Worker worker = workerToRemove.get();

            // Промяна на статуса на работника на isStopped = true
            worker.setStopped(true);

            // Прекратяване на работника
            worker.stopWorker();

            // Премахване на работника от списъка
            workers.remove(worker);

            System.out.println("Worker with ID " + workerId + " has been removed.");
        } else {
            System.out.println("Worker with ID " + workerId + " not found.");
        }
    }

    @Override
    public void startGame(int initialMineResources, int initialWorkers) {
        this.totalResourcesInMine = initialMineResources;
        for (int i = 1; i <= initialWorkers; i++) {
            addWorker();
        }
    }

    @Override
    public void stopGame() {
        executor.shutdownNow();
        List<Worker> workers = getWorkers();
        workers.clear();
    }

    public int getTotalResourcesInMine() {
        return totalResourcesInMine;
    }

    public void setTotalResourcesInMine(int totalResourcesInMine) {
        this.totalResourcesInMine = totalResourcesInMine;
        System.out.println(totalResourcesInMine+" Left");
    }

    public void broadcastWorkers() {
        List<Worker> workers = getWorkers();
        messagingTemplate.convertAndSend("/topic/workers", workers);
    }
}
