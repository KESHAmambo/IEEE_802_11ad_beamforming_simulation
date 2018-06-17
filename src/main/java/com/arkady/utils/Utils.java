package com.arkady.utils;

import com.arkady.model.CollisionPretender;
import com.arkady.model.Connection;
import com.arkady.model.SimulationConfig;
import com.arkady.simulation.SimulationService;

import java.util.Map;

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


    public static boolean isSnrAcceptable(String testedStationId, Map<String, CollisionPretender> collisionPretenders) {
        CollisionPretender testedCollisionPretender = collisionPretenders.get(testedStationId);
        try {
            testedCollisionPretender.checked.set(true);

            double testedStationPower = testedCollisionPretender.connection.power;

            double otherStationSumPower = 0;
            for (Map.Entry<String, CollisionPretender> entry : collisionPretenders.entrySet()) {
                if (!entry.getKey().equals(testedStationId)) {
                    otherStationSumPower += entry.getValue().connection.power;
                }
            }

            double powerRation = testedStationPower / (SimulationConfig.noisePower + otherStationSumPower);
            boolean signalAccepted = powerRation > SimulationConfig.minPowerRatio;

            System.out.println("Collision pretenders: " + collisionPretenders.size() +
                    " " + testedStationId +
                    " signal Accepted: " + signalAccepted);

            return signalAccepted;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void clearCollisionPretendersIfChecked(
            Map<String, CollisionPretender> collisionPretenders) {

        boolean allCollisionPretendersChecked = true;
        for(Map.Entry<String, CollisionPretender> entry: collisionPretenders.entrySet()) {
            allCollisionPretendersChecked = entry.getValue().checked.get();
            if(!allCollisionPretendersChecked) {
                break;
            }
        }
        if(allCollisionPretendersChecked) {
            collisionPretenders.clear();
        }
    }
}
