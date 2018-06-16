package com.arkady.model.frames;

import com.arkady.model.frames.Frame;

/**
 * Created by abara on 16.05.2018.
 */
public class BeaconFrame extends Frame {
    public static final double REAL_BEACON_INTERVAL_DURATION = 102.4;
    // beacon interval scaled (102.4 ms in real) 102400 mcs
    public static final long BEACON_INTERVAL_DURATION = 4000;
    // SIZE in bytes
    public static final int SIZE = 512;

    // in micro seconds
    public static final int BTI_DURATION = 3100;
    public static final int SLS_SLOTS = 4;
    public static final int ABFT_DURATION = SswFrame.SLS_SLOT_DURATION * SLS_SLOTS; //
    public static final int ATI_DURATION = 2000;

    public final long beaconIntervalStartTime;
    public final long beaconIntervalEndTime;
    public final long abftStartTime;
    public final long atiStartTime;
    public final long dtiStartTime;
    public final String initiatorStationId;
    public final int sectorNumber;
    public final int beaconIntervalNumber;

    public BeaconFrame(
            long beaconIntervalStartTime,
            long beaconIntervalEndTime,
            long abftStartTime,
            long atiStartTime,
            long dtiStartTime,
            String initiatorStationId,
            int sectorNumber,
            int beaconIntervalNumber) {
        this.beaconIntervalStartTime = beaconIntervalStartTime;
        this.beaconIntervalEndTime = beaconIntervalEndTime;
        this.abftStartTime = abftStartTime;
        this.atiStartTime = atiStartTime;
        this.dtiStartTime = dtiStartTime;
        this.initiatorStationId = initiatorStationId;
        this.sectorNumber = sectorNumber;
        this.beaconIntervalNumber = beaconIntervalNumber;
    }
}
