package serezliev.MiningGame.services;

public interface Worker {

    int getId();

    int getTotalMinedResources();

    double getTotalReceivedMoney();

    int getTotalWorkingTime();

    int getTotalRestingTime();

    int getTotalResourcesLeft();

    int setTotalResourcesLeft(int totalLeftResources);

    boolean isStopped();

    void setStopped(boolean stopped);

    void startMining();

    void stopMining();

    void startResting();

    void stopResting();

    void stopWorker();

    void paySalary();
}
