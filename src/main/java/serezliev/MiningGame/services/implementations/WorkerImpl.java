package serezliev.MiningGame.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import serezliev.MiningGame.services.Worker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkerImpl implements Worker, Runnable {

    private static final AtomicInteger idGenerator = new AtomicInteger(1);
    private final int id;
    private int totalMinedResources;
    private int totalReceivedMoney;
    private int totalWorkingTime;
    private int totalRestingTime;
    private String actionMessage;
    private volatile boolean isStopped = false;
    private volatile boolean mineExhausted = false;
    private MiningGameServiceImpl miningGameService;
    private final SimpMessagingTemplate messagingTemplate;


    // Инжектиране на MiningGameService при създаване на работник
    @Autowired
    public WorkerImpl(MiningGameServiceImpl miningGameService,
                      SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.id = idGenerator.getAndIncrement();
        this.miningGameService = miningGameService;
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

    public String getActionMessage() {
        return actionMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    @Override
    public void startMining() {
        System.out.println("Worker " + id + " is mining...");
        totalMinedResources += 10;
        totalWorkingTime += 5;
        miningGameService.setTotalResourcesInMine(miningGameService.getTotalResourcesInMine()-10);
        setActionMessage("Worker " + id + " is mining...");
        miningGameService.broadcastWorkers();
    }

    @Override
    public void stopMining() {
        miningGameService.broadcastWorkers();
    }

    @Override
    public void startResting() {
        System.out.println("Worker " + id + " is resting...");
        totalRestingTime += 3;
        messagingTemplate.convertAndSend("/topic/workers", "Worker " + id + " is resting...");
        setActionMessage( "Worker " + id + " is resting...");
        miningGameService.broadcastWorkers();

    }

    @Override
    public void stopResting() {
        miningGameService.broadcastWorkers();
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
        setActionMessage("Worker " + id + " has received $" + salary + " for total " + totalMinedResources + " mined resources");
        miningGameService.broadcastWorkers();
    }

}
