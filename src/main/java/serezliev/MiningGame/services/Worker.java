package serezliev.MiningGame.services;

import org.springframework.stereotype.Service;

@Service
public interface Worker {

    int getId();

    int getTotalMinedResources();

    int getTotalReceivedMoney();

    int getTotalWorkingTime();

    int getTotalRestingTime();

    boolean isStopped();

    void startMining();

    void stopMining();

    void startResting();

    void stopResting();

    void stopWorker();

    void paySalary();
}
