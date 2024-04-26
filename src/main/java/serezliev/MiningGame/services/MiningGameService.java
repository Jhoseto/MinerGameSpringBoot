package serezliev.MiningGame.services;


import java.util.List;

public interface MiningGameService {

    List<Worker> getWorkers();

    void addWorker();

    void removeWorker(int workerId);

    void startGame(int initialMineResources, int initialWorkers);

    void stopGame();

    void restartGame();

    void finishMining();

    int getTotalResourcesInMine();

    void broadcastWorkers();

    boolean isPaused();

    void setFinish(boolean finish);

    void pauseGame();

    void resumeGame();
}
