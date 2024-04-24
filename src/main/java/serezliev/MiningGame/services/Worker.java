package serezliev.MiningGame.services;

public interface Worker {

    int getId();

    int getTotalMinedResources();

    int getTotalReceivedMoney();

    int getTotalWorkingTime();

    int getTotalRestingTime();

    int getTotalResourcesLeft();

    boolean isStopped();

    void setStopped(boolean stopped);

    void startMining();

    void stopMining();

    void startResting();

    void stopResting();

    void stopWorker();

    void paySalary();
}
