package com.arkady.model.frames;

import com.arkady.model.frames.Frame;

/**
 * Created by abara on 16.05.2018.
 */
public class BeaconFrame extends Frame {
    // beacon interval scaled (102.4 ms in real) 1024000
    public static final long BEACON_INTERVAL_DURATION = 6000;
    // SIZE in bytes
    public static final int SIZE = 512;

    // in micro seconds
    public static final int BTI_DURATION = 3100;
    public static final int ABFT_DURATION = 400; //
    public static final int ATI_DURATION = 2000;

    public final long beaconIntervalStartTime;
    public final long beaconIntervalEndTime;
    public final long abftStartTime;
    public final long atiStartTime;
    public final long dtiStartTime;
    public final String initiatorStationId;
    public final int sectorNumber;

    public BeaconFrame(
            long beaconIntervalStartTime,
            long beaconIntervalEndTime,
            long abftStartTime,
            long atiStartTime,
            long dtiStartTime,
            String initiatorStationId,
            int sectorNumber) {
        this.beaconIntervalStartTime = beaconIntervalStartTime;
        this.beaconIntervalEndTime = beaconIntervalEndTime;
        this.abftStartTime = abftStartTime;
        this.atiStartTime = atiStartTime;
        this.dtiStartTime = dtiStartTime;
        this.initiatorStationId = initiatorStationId;
        this.sectorNumber = sectorNumber;
    }
}
