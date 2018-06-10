package com.arkady.utils;

import com.arkady.simulation.SimulationService;
import com.arkady.model.Connection;
import com.arkady.model.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Created by abara on 27.05.2018.
 */
public class BeaconIntervalBarrierAction implements Runnable {
    public static Double MOBILITY_QUOTIENT = 0.2;
    public static Boolean SectorsPowerDistributionIndependent = false;
    public static final double[] adjacentSectorsPowerQuotients = {1, (double) 1 / 3, (double) 1 / 9, (double) 1 / 27};
    
    public static Map<String, Map<String, Map<Integer, Connection>>> connections=
            new ConcurrentHashMap<>();

    public static volatile AtomicInteger beaconIntervalNumber = new AtomicInteger();
    private boolean firstIterationFlag = true;

    public void run() {
        System.out.println("Barrier started, BI " + beaconIntervalNumber.incrementAndGet());

        if(connections.isEmpty()) {
            SimulationService.stations.entrySet().forEach(this::setNewPosition);
        } else {
            SimulationService.stations.entrySet().stream()
                    .filter(entryA -> Math.random() < MOBILITY_QUOTIENT).forEach(this::setNewPosition);
        }

        try {
            if(!firstIterationFlag) {
                sleep(6000);
            }
            firstIterationFlag = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Barrier finished");
        System.out.println();
    }

    private void setNewPosition(Map.Entry<String, Station> entryA) {
        Station stationA = entryA.getValue();
        String stationAId = stationA.getStationId();
        Integer stationANumberOfSectors = stationA.numberOfSectors;

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

                if(SectorsPowerDistributionIndependent) {
                    defineSectorsPowerIndependently(
                            stationANumberOfSectors, stationAId, stationBId, aToBSectorsConnections);
                } else {
                    defineSectorsPowerDueToAdjacentQuotients(
                            stationANumberOfSectors, stationAId, stationBId, aToBSectorsConnections);
                }
            }
        }
    }

    private void defineSectorsPowerDueToAdjacentQuotients(
            Integer stationANumberOfSectors,
            String stationAId,
            String stationBId,
            Map<Integer, Connection> aToBSectorsConnections) {

        int mostPowerfulSector = chooseMostPowerfulSector(stationANumberOfSectors);
        double mostPowerfulSectorPower = Connection.getRandomPower();
        System.out.println("Init: " + stationAId + " most powerful sector: " + mostPowerfulSector +
                " " + mostPowerfulSectorPower);

        for(int sectorNumber = 0; sectorNumber < stationANumberOfSectors; sectorNumber++) {
            int diffWithMostPowerfulSectorNumber = calculateDiffWithMostPowerfulSectorNumber(
                    stationANumberOfSectors, mostPowerfulSector, sectorNumber);

            double sectorPower;
            if(diffWithMostPowerfulSectorNumber < 4) {
                sectorPower = mostPowerfulSectorPower * adjacentSectorsPowerQuotients[diffWithMostPowerfulSectorNumber];
            } else {
                sectorPower = 0;
            }
            aToBSectorsConnections.put(sectorNumber, new Connection(sectorNumber, sectorPower));
            System.out.println("Init: " + stationAId + " " + stationBId +
                    " " + sectorNumber + " " + sectorPower);
        }
        System.out.println();
    }

    private int calculateDiffWithMostPowerfulSectorNumber(Integer stationANumberOfSectors, int mostPowerfulSector, int sectorNumber) {
        return Math.min(
                        Math.min(
                                Math.abs(mostPowerfulSector - sectorNumber),
                                Math.abs(mostPowerfulSector - sectorNumber - stationANumberOfSectors)),
                        Math.abs(mostPowerfulSector - sectorNumber + stationANumberOfSectors));
    }

    private void defineSectorsPowerIndependently(
            Integer stationANumberOfSectors,
            String stationAId,
            String stationBId,
            Map<Integer, Connection> aToBSectorsConnections) {

        for(int sectorNumber = 0; sectorNumber < stationANumberOfSectors; sectorNumber++) {
            aToBSectorsConnections.put(sectorNumber, new Connection(sectorNumber));
            System.out.println("Init: " + stationAId + " " + stationBId + " " + sectorNumber);
        }
        System.out.println();
    }

    private int chooseMostPowerfulSector(int numberOfSectors) {
        double rand = Math.random() * numberOfSectors;
        return (int) Math.floor(rand);
    }
}
