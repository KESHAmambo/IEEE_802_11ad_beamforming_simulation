package com.arkady.model;

/**
 * Created by abara on 27.05.2018.
 */
public class SimulationConfig {
    public int numberOfMobileStations;
    public int numberOfAccessPoints = 1;

    public SimulationConfig(int numberOfMobileStations) {
        this.numberOfMobileStations = numberOfMobileStations;
    }

    public SimulationConfig(int numberOfMobileStations, int numberOfAccessPoints) {
        this.numberOfMobileStations = numberOfMobileStations;
        this.numberOfAccessPoints = numberOfAccessPoints;
    }
}
