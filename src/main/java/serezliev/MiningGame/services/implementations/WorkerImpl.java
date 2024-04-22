package serezliev.MiningGame.services.implementations;

import serezliev.MiningGame.services.Worker;

import java.util.concurrent.TimeUnit;


public class WorkerImpl implements Worker, Runnable {

    private final int id;
    private int totalMinedResources;
    private int totalReceivedMoney;
    private int totalWorkingTime;
    private int totalRestingTime;
    private volatile boolean isStopped = false;
    private volatile boolean mineExhausted = false;
    private MiningGameServiceImpl miningGameService;



    public WorkerImpl(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (!mineExhausted && !isStopped) {
                if (miningGameService.getTotalResourcesInMine() <= 0) {
                    mineExhausted = true;
                    break;
                }
                startMining();
                TimeUnit.SECONDS.sleep(5);
                stopMining();
                startResting();
                TimeUnit.SECONDS.sleep(3);
                paySalary();
                stopResting();
            }
        } catch (InterruptedException e) {
            System.out.println("Worker " + id + " has left");
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
    public int getTotalReceivedMoney() {
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
    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public void startMining() {
        System.out.println("Worker " + id + " is mining...");
        totalMinedResources += 10;
        totalWorkingTime += 5;
        miningGameService.setTotalResourcesInMine(miningGameService.getTotalResourcesInMine()-10);
    }

    @Override
    public void stopMining() {
        System.out.println("Worker " + id + " has finished mining.");
    }

    @Override
    public void startResting() {
        System.out.println("Worker " + id + " is resting...");
        totalRestingTime += 3;
    }

    @Override
    public void stopResting() {
        System.out.println("Worker " + id + " has finished resting.");
    }

    @Override
    public void stopWorker() {
        isStopped = true;
    }

    @Override
    public void paySalary() {
        int salary = 10 / 2; // 2.5 $ for every 5 sec on work
        totalReceivedMoney += salary;
        System.out.println("Worker " + id + " has received $" + salary + " for total " + totalMinedResources + " mined resources");
    }
}
