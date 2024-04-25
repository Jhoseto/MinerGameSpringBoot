package serezliev.MiningGame.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import serezliev.MiningGame.services.Worker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkerImpl implements Worker, Runnable {

    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private final int id;
    private int totalMinedResources;
    private double totalReceivedMoney;
    private int totalWorkingTime;
    private int totalRestingTime;
    private int totalResourcesLeft;
    private String actionMessage;
    private volatile boolean isStopped = false;
    private final MiningGameServiceImpl miningGameService;


    @Autowired
    public WorkerImpl(MiningGameServiceImpl miningGameService) {
        this.id = idGenerator.getAndIncrement();
        this.miningGameService = miningGameService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (miningGameService.getTotalResourcesInMine() <= 0) {
                    miningGameService.finishMining();
                    setStopped(true);
                    miningGameService.broadcastWorkers();
                   break;
                }else if (!miningGameService.isPaused()){
                    startMining();
                    TimeUnit.SECONDS.sleep(5);
                    stopMining();
                    paySalary();
                    startResting();
                    TimeUnit.SECONDS.sleep(3);
                    stopResting();

                    miningGameService.broadcastWorkers();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Worker " + id + " has left");
            totalResourcesLeft = miningGameService.getTotalResourcesInMine();
            miningGameService.broadcastWorkers();
        }
    }

    @Override
    public int getId() {
        return id;
    }
    @Override
    public int getTotalMinedResources() {
        return totalMinedResources;
    }
    @Override
    public double getTotalReceivedMoney() {
        return totalReceivedMoney;
    }
    @Override
    public int getTotalWorkingTime() {
        return totalWorkingTime;
    }
    @Override
    public int getTotalRestingTime() {
        return totalRestingTime;
    }
    @Override
    public int getTotalResourcesLeft() {
        return totalResourcesLeft;
    }
    @Override
    public int setTotalResourcesLeft(int totalLeftResources) {
        return totalResourcesLeft;
    }
    @Override
    public boolean isStopped() {
        return isStopped;
    }
    @Override
    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }


    public String getActionMessage() {
        return actionMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }


    @Override
    public void startMining() {
        System.out.println(" Worker " + id + "Mining...");
        setActionMessage("Mining...");
        totalResourcesLeft = miningGameService.getTotalResourcesInMine();
        miningGameService.broadcastWorkers();
    }

    @Override
    public void stopMining() {

        totalWorkingTime += 5;
        totalMinedResources += 10;
        miningGameService.setTotalResourcesInMine(miningGameService.getTotalResourcesInMine()-10);
        totalResourcesLeft = miningGameService.getTotalResourcesInMine();
        miningGameService.broadcastWorkers();
    }

    @Override
    public void startResting() {
        System.out.println(" Worker " + id + " Resting...");
        setActionMessage( "Resting...");
        totalResourcesLeft = miningGameService.getTotalResourcesInMine();
        miningGameService.broadcastWorkers();

    }

    @Override
    public void stopResting() {
        totalRestingTime += 3;
        totalResourcesLeft = miningGameService.getTotalResourcesInMine();
        miningGameService.broadcastWorkers();
    }

    @Override
    public void stopWorker() {
        isStopped = true;
        setActionMessage("Left the mine...");
    }

    @Override
    public void paySalary() {
        double salary = 2.5;  // 2.5 $ for every 5 sec on work
        totalReceivedMoney += salary;
        System.out.println("Worker " + id + " has received $" + salary + " for total " + totalMinedResources + " mined resources");
        miningGameService.broadcastWorkers();
    }

}
