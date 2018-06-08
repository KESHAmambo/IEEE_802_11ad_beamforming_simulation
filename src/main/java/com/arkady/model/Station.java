package com.arkady.model;

import com.arkady.simulation.SimulationService;
import com.arkady.model.frames.BeaconFrame;
import com.arkady.model.frames.SswFrame;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by abara on 13.05.2018.
 */
public abstract class Station extends Thread {
    protected final SimulationService simulationService;
    public CyclicBarrier barrier;
    private final String stationId;
    public final Integer numberOfSectors;

    public AtomicBoolean transmitting = new AtomicBoolean(false);
    public AtomicBoolean receiving = new AtomicBoolean(false);
    public AtomicBoolean receivingCollision = new AtomicBoolean(false);

    public AtomicBoolean positionChanged = new AtomicBoolean(false);
    public volatile BeaconFrame currentBeaconFrame;

    public Map<String, Connection> bestReceivedConnections = new ConcurrentHashMap<>();
    public Map<String, Connection> bestTransmittedConnections = new ConcurrentHashMap<>();

    public Station(String stationId, CyclicBarrier barrier,
                   SimulationService simulationService, Integer numberOfSectors) {
        this.stationId = stationId;
        this.barrier = barrier;
        this.simulationService = simulationService;
        this.numberOfSectors = numberOfSectors;
    }

    public String getStationId() {
        return stationId;
    }

    // waiting for first barrier action to initialize stations positions
    protected void waitFirstBarrierAction() {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }


    public abstract void receiveBeacon(BeaconFrame beaconFrame);

    public abstract void receiveSswFrame(SswFrame sswFrame);

    public abstract void receiveSswFeedback(SswFrame sswFrame);

    public abstract void receiveSswAck(SswFrame sswFrame);


    protected void awaitBarrier() {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}

