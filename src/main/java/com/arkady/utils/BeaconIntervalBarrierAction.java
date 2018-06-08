package com.arkady.utils;

import com.arkady.simulation.SimulationService;
import com.arkady.model.Connection;
import com.arkady.model.Station;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

/**
 * Created by abara on 27.05.2018.
 */
public class BeaconIntervalBarrierAction implements Runnable {
    public static final Double MOBILITY_QUOTIENT = 0.2;
    public static Map<String, Map<String, Map<Integer, Connection>>> connections=
            new ConcurrentHashMap<>();

    boolean flag = true;

    public void run() {
        System.out.println("Barrier started");

        if(connections.isEmpty()) {
            SimulationService.stations.entrySet().forEach(this::setNewPosition);
        } else {
            SimulationService.stations.entrySet().stream()
                    .filter(entryA -> Math.random() < MOBILITY_QUOTIENT).forEach(this::setNewPosition);
        }

        try {
            if(!flag) {
                sleep(6000);
            }
            flag = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Barrier finished");
        System.out.println();
    }

    private void setNewPosition(Map.Entry<String, Station> entryA) {
        Station stationA = entryA.getValue();
        String stationAId = stationA.getStationId();

        stationA.positionChanged.set(true);

        Map<String, Map<Integer, Connection>> connectionsForStationA =
                new ConcurrentHashMap<>();
        connections.put(stationAId, connectionsForStationA);

        for(Map.Entry<String, Station> entryB: SimulationService.stations.entrySet()) {
            Station stationB = entryB.getValue();
            String stationBId = stationB.getStationId();

            if(!stationBId.equals(stationAId)) {
                // map for station A transmission sectors to station B
                Map<Integer, Connection> aToBSectorsConnections = new ConcurrentHashMap<>();
                connectionsForStationA.put(stationBId, aToBSectorsConnections);

                for(int sectorNumber = 0; sectorNumber < stationA.numberOfSectors; sectorNumber++) {
                    aToBSectorsConnections.put(sectorNumber, new Connection(sectorNumber));
                    System.out.println("Init: " + stationAId + " " + stationBId + " " + sectorNumber);
                }
                System.out.println();
            }
        }
    }
}
