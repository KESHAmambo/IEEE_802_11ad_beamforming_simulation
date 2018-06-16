package com.arkady.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by abara on 31.05.2018.
 */
public class Connection {
    // power of signal in db
    private static final Double minPower = 80.0;
    private static final Double maxPower = 100.0;

    public final Double power;
    public final Integer sectorNumber;
    public AtomicBoolean newConnection = new AtomicBoolean(true);
    public AtomicBoolean confirmed = new AtomicBoolean(false);
    public int beaconIntervalNumber;

    public Connection(int sectorNumber) {
        this.sectorNumber = sectorNumber;
        this.power = getRandomPower();
    }

    public Connection(int sectorNumber, Double power) {
        this.sectorNumber = sectorNumber;
        this.power = power;
    }

    public static Double getRandomPower() {
        return minPower + (maxPower - minPower) * Math.random();
    }
}
