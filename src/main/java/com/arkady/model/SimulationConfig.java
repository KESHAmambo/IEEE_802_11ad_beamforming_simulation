package com.arkady.model;

/**
 * Created by abara on 27.05.2018.
 */
public class SimulationConfig {
    public int numberOfMobileStations;
    public int numberOfAccessPoints = 1;
    // noise power in dB, for 2.16 GHz approximately -42 dB
    public static int noisePower = 81;
    public static int acceptableBer = 7;
    public static double minRequiredSnr = 1.5;

    public SimulationConfig(int numberOfMobileStations) {
        this.numberOfMobileStations = numberOfMobileStations;
    }

    public SimulationConfig(int numberOfMobileStations, int numberOfAccessPoints) {
        this.numberOfMobileStations = numberOfMobileStations;
        this.numberOfAccessPoints = numberOfAccessPoints;
    }
}
