package me.awie1000.riddlebot;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class ScavengerHunt {

    final static MaterialClassifier mats = new MaterialClassifier();
    HashMap<Material, Set<UUID>> huntLog;
    HashMap<UUID, Integer> playerCounts;
    boolean isRunning, isFinished;

    public ScavengerHunt() {
        huntLog = new HashMap<>();
        playerCounts = new HashMap<>();
        isRunning = false;
        isFinished = true;
    }

    public int size() {
        return huntLog.size();
    }

    public void start() throws ScavengerError {
        if(isFinished) throw new ScavengerError("Cannot start a hunt that has been completed. \nPlease either regenerate or restart hunt.");
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public void end() {
        isRunning = false;
        isFinished = true;
    }

    public void restart() {
        this.isRunning = false;
        huntLog.replaceAll((m, v) -> new HashSet<>());
        playerCounts.clear();
        this.isFinished = false;
        this.isRunning = true;
    }

    public HashMap<UUID, Integer> getAllPlayerObjectiveCount() {
        return playerCounts;
    }

    public int getPlayerObjectiveCount(Player player) {
        if(!playerCounts.containsKey(player.getUniqueId())) return 0;
        return playerCounts.get(player.getUniqueId());
    }

    public void addMaterialToList(Material mat) throws ScavengerError {
        if(isFinished) throw new ScavengerError("Cannot add to a completed hunt. \nPlease either regenerate or restart hunt.");
        if(huntLog.containsKey(mat)) throw new ScavengerError(String.format("Hunt already has %s.", MaterialClassifier.matToName(mat)));
        huntLog.put(mat, new HashSet<>());
    }

    public void addPlayerToMaterial(Material mat, Player player) throws ScavengerError {
        if(isFinished) throw new ScavengerError("Current hunt has finished.");
        if(!isRunning) throw new ScavengerError("Hunt is not running.");
        if(!huntLog.containsKey(mat)) throw new ScavengerError(String.format("Hunt does not have %s.", MaterialClassifier.matToName(mat)));
        if(huntLog.get(mat).contains(player.getUniqueId())) throw new ScavengerError(String.format("You've already submitted %s.", MaterialClassifier.matToName(mat)));
        huntLog.get(mat).add(player.getUniqueId());
        playerCounts.put(player.getUniqueId(), this.getPlayerObjectiveCount(player) + 1);
    }

    public void generate(int amount) throws ScavengerError {
        if(isRunning) throw new ScavengerError("Cannot regenerate list while hunt is running.");
        if(amount <= 0) throw new ScavengerError("Cannot start a hunt with that amount of items.");
        huntLog.clear();
        for(Material mat : mats.randomSurvivalList(amount)) {
            huntLog.put(mat, new HashSet<>());
        }
        playerCounts.clear();
        isFinished = false;
    }

    public HashMap<Material, Boolean> getPlayerProgress(Player player) {
        HashMap<Material, Boolean> playerMap = new HashMap<>();
        for(Material mat : huntLog.keySet()) {
            playerMap.put(mat, huntLog.get(mat).contains(player.getUniqueId()));
        }
        return playerMap;
    }
}
