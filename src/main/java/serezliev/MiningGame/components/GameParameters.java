package serezliev.MiningGame.components;

public class GameParameters {
    private int initialMineResources;
    private int initialWorkers;

    // Гетъри и сетъри
    public int getInitialMineResources() {
        return initialMineResources;
    }

    public void setInitialMineResources(int initialMineResources) {
        this.initialMineResources = initialMineResources;
    }

    public int getInitialWorkers() {
        return initialWorkers;
    }

    public void setInitialWorkers(int initialWorkers) {
        this.initialWorkers = initialWorkers;
    }
}
