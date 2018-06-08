package com.arkady.model.frames;

import com.arkady.model.Connection;

/**
 * Created by abara on 16.05.2018.
 */
public class SswFrame extends  Frame {
    public static final int SLS_SLOT_DURATION = 50;
    // SIZE in bytes
    public static final int SIZE = 16;

    public final int initiatorSectorNumber;
    public final Connection bestReceivedConnection;
    public final String initiatorStationId;
    public final boolean lastSector;
    public final long slsSlotEndTime;

    public SswFrame(int initiatorSectorNumber, Connection bestReceivedConnection, String initiatorStationId, boolean lastSector, long slsSlotEndTime) {
        this.initiatorSectorNumber = initiatorSectorNumber;
        this.bestReceivedConnection = bestReceivedConnection;
        this.initiatorStationId = initiatorStationId;
        this.lastSector = lastSector;
        this.slsSlotEndTime = slsSlotEndTime;
    }
}
