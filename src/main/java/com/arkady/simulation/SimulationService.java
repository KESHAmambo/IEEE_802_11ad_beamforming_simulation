package com.arkady.simulation;

import com.arkady.model.AccessPoint;
import com.arkady.model.MobileDevice;
import com.arkady.model.SimulationConfig;
import com.arkady.model.Station;
import com.arkady.utils.BeaconIntervalBarrier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;

/**
 * Created by abara on 13.05.2018.
 */

@Service
public class SimulationService {
    // speed of light m/ms
    private static final int c = 300000;

    /*
    the TIME_SCALE for each time period
     */
    public static final int TIME_SCALE = 10000;

    private SimulationConfig config;
    public AtomicBoolean ended = new AtomicBoolean(true);

    public static Map<String, Station> stations;
    public static Map<String, MobileDevice> mobileDevices;

    public SimulationConfig getConfig() {
        return config;
    }

    public void setConfig(SimulationConfig config) {
        this.config = config;
    }

    public Map<String, Station> getStations() {
        return stations;
    }

    public void addNode(Station station) {
        stations.put(station.getStationId(), station);
    }

    public Boolean startSimulation() {
        if(this.ended.get()) {
            CyclicBarrier barrier = createBarrier();
            createStations(barrier);
            this.ended.set(false);
            System.out.println("Simulation started");
            return true;
        } else {
            return false;
        }
    }

    private CyclicBarrier createBarrier() {
        BeaconIntervalBarrier barrier = new BeaconIntervalBarrier(
                config.numberOfMobileStations + config.numberOfAccessPoints);
        return barrier;
    }

    private void createStations(CyclicBarrier barrier) {
        stations = new ConcurrentHashMap<>();
        mobileDevices = new ConcurrentHashMap<>();

        AccessPoint accessPoint = new AccessPoint(barrier, this);
        stations.put(accessPoint.getStationId(), accessPoint);

        MobileDevice mobileDevice;
        for(int i = 0; i < config.numberOfMobileStations; i++) {
            mobileDevice = new MobileDevice(barrier, this);
            stations.put(mobileDevice.getStationId(), mobileDevice);
            mobileDevices.put(mobileDevice.getStationId(), mobileDevice);
            mobileDevice.start();
        }

        accessPoint.start();
    }

    // "ended" field should be reset by the threads when they are all finished.
    // Current way of setting ended = true is not concurrently safe as new simulaiton
    // could be started before the end of old one.
    public void endSimulation() {
        this.ended.set(true);
    }
}
