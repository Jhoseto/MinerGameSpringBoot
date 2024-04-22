package serezliev.MiningGame.services.implementations;

import org.springframework.stereotype.Service;
import serezliev.MiningGame.services.MiningGameService;
import serezliev.MiningGame.services.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MiningGameServiceImpl implements MiningGameService {

    private static final int MAX_MINE_SIZE = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_MINE_SIZE);

    private int totalResourcesInMine = 0;
    private final List<Worker> workers = new ArrayList<>();

    @Override
    public List<Worker> getWorkers() {
        return workers;
    }

    @Override
    public void addWorker() {
        if (workers.size() < MAX_MINE_SIZE) {
            int newWorkerId = workers.size() + 1;
            Worker newWorker = new WorkerImpl(newWorkerId);
            workers.add(newWorker);
            executor.submit((Runnable) newWorker);
        }
    }

    @Override
    public void removeWorker(int workerId) {
        workers.stream()
                .filter(worker -> worker.getId() == workerId)
                .findFirst()
                .ifPresent(Worker::stopMining);
        workers.removeIf(worker -> worker.getId() == workerId);
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
    }

    public int getTotalResourcesInMine() {
        return totalResourcesInMine;
    }

    public void setTotalResourcesInMine(int totalResourcesInMine) {
        this.totalResourcesInMine = totalResourcesInMine;
    }
}
