package com.arkady.utils;

import java.util.concurrent.CyclicBarrier;

import static java.lang.Thread.sleep;

/**
 * Created by abara on 27.05.2018.
 */
public class BeaconIntervalBarrier extends CyclicBarrier {
    public BeaconIntervalBarrier(int parties, Runnable barrierAction) {
        super(parties, barrierAction);
    }

    public BeaconIntervalBarrier(int parties) {
        super(parties, new BeaconIntervalBarrierAction());
    }
}
