package com.arkady.model;

/**
 * Created by abara on 27.05.2018.
 */
public class SimulationConfig {
    public int numberOfMobileStations;
    public int numberOfAccessPoints = 1;
    // noise power in dB, for 2.16 GHz approximately in linear scale
    public static double noisePower = 7.9e-9;
    // calculated from acceptable BER of 7 dB
    public static double minPowerRatio = 5.0;
//    public static double minRequiredSnr = 1.5;

    public SimulationConfig(int numberOfMobileStations) {
        this.numberOfMobileStations = numberOfMobileStations;
    }

    public SimulationConfig(int numberOfMobileStations, int numberOfAccessPoints) {
        this.numberOfMobileStations = numberOfMobileStations;
        this.numberOfAccessPoints = numberOfAccessPoints;
    }
}
