package com.arkady.utils;

import com.arkady.simulation.SimulationService;

import static java.lang.Thread.sleep;

/**
 * Created by abara on 16.05.2018.
 */
public class Utils {
    public static boolean enoughToTransmit(int size, long intervalEndTime, int speed) {
        long currentTime = System.currentTimeMillis();
        double timeRemained = (double) (intervalEndTime - currentTime);
        double timeToTransmit = (double) size / speed * SimulationService.TIME_SCALE;
        return timeRemained > timeToTransmit;
    }

    public static long calculateTimeToTransmitInScale(int size, long speed) {
        return (long) ((double) size / speed * SimulationService.TIME_SCALE);
    }

    public static boolean isTransmissionEnded(long transmissionStartTime, int size, int speed) {
        long currentTime = System.currentTimeMillis();
        long timeToTransmit = size / speed * SimulationService.TIME_SCALE;
        long transmissionEndTime = transmissionStartTime + timeToTransmit;
        return currentTime > transmissionEndTime;
    }

    public static void waitTillTransmissionEnd(int size, long speed) {
        long timeToTransmitInScale = Utils.calculateTimeToTransmitInScale(size, speed);
        try {
            sleep(timeToTransmitInScale);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void waitTillTime(long timeToWait) {
        long currentTime = System.currentTimeMillis();
        while(currentTime < timeToWait) {
            try {
                sleep(1);
                currentTime = System.currentTimeMillis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
