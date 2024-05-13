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
    private static ExecutorService executor = Executors.newFixedThreadPool(MAX_MINE_SIZE);
    private volatile int totalResourcesInMine = 0;
    private final List<Worker> workers = new ArrayList<>();
    private final SimpMessagingTemplate messagingTemplate;
    private volatile boolean paused = false;
    private volatile boolean isFinish = false;



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
            Worker newWorker = new WorkerImpl(this);
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
        setTotalResourcesInMine(initialMineResources);
        if (paused) {
            paused = false; // Ако играта е паузирана, я продължаваме
        }
        for (int i = 1; i <= initialWorkers; i++) {
            addWorker();
        }
    }


    @Override
    public void stopGame() {

        pauseGame();
    }

    @Override
    public void restartGame() {
        executor.shutdownNow();
        workers.clear();
        executor = Executors.newFixedThreadPool(MAX_MINE_SIZE);
        broadcastWorkers();
    }

    @Override
    public void finishMining() {
        setFinish(true);
        executor.shutdownNow();
        workers.forEach(Worker::stopWorker);
        broadcastWorkers();

    }

    public int getTotalResourcesInMine() {
        return totalResourcesInMine;
    }

    public void setTotalResourcesInMine(int totalResourcesInMine) {
        this.totalResourcesInMine = totalResourcesInMine;
        System.out.println(totalResourcesInMine+" Left");
        broadcastWorkers();
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public synchronized void broadcastWorkers() {
        List<Worker> workers = getWorkers();
        messagingTemplate.convertAndSend("/topic/workers", workers);
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void pauseGame() {
        paused = true;
    }



    @Override
    public void resumeGame() {
        paused = false;
    }

}
