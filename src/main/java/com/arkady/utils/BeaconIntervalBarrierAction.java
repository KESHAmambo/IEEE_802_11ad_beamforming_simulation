package com.arkady.utils;

import com.arkady.model.MobileDevice;
import com.arkady.simulation.SimulationService;
import com.arkady.model.Connection;
import com.arkady.model.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Created by abara on 27.05.2018.
 */
public class BeaconIntervalBarrierAction implements Runnable {
    public static Double MOBILITY_QUOTIENT = 0.0;
    public static Boolean SectorsPowerDistributionIndependent = false;
    public static final double[] adjacentSectorsPowerQuotients = {1, (double) 1 / 3, (double) 1 / 9, (double) 1 / 27};
    
    public static Map<String, Map<String, Map<Integer, Connection>>> connections=
            new ConcurrentHashMap<>();

    public static final int numberOfExperiments = 50;
    public static volatile AtomicBoolean beamformingSucceeded = new AtomicBoolean(false);
    public static List<Double> averageBeamformingTimes = new ArrayList<>();
    public static double maxBeamformingDuration = 0;

    public static volatile AtomicInteger beaconIntervalNumber = new AtomicInteger();
    private boolean firstIterationFlag = true;

    public void run() {
        System.out.println("Barrier started, BI " + beaconIntervalNumber.incrementAndGet());

        if(beamformingSucceeded.get()) {
            beamformingSucceeded.set(false);

            System.out.println("Experiment " + averageBeamformingTimes.size());
            System.out.println("All Mobile Devices ended beamforming");
            double beamformingSumTime = 0;
            for (Map.Entry<String, MobileDevice> entry: SimulationService.mobileDevices.entrySet()) {
                MobileDevice mobileDevice = entry.getValue();

                if(maxBeamformingDuration < mobileDevice.beamformingDuration) {
                    maxBeamformingDuration = mobileDevice.beamformingDuration;
                }

                beamformingSumTime+= mobileDevice.beamformingDuration;
                System.out.println(mobileDevice.getStationId() + " " + mobileDevice.beamformingDuration);

                mobileDevice.beamformingSuccess = false;
                mobileDevice.beamformingDuration = 0;
                mobileDevice.beaconsCount = 0;
            }
            double averageBeamformingTime = beamformingSumTime / SimulationService.mobileDevices.size();
            System.out.println("Beamforming average time = " + averageBeamformingTime);

            averageBeamformingTimes.add(averageBeamformingTime);

            if(averageBeamformingTimes.size() == numberOfExperiments) {
                System.out.println("-----------------------------------------");
                System.out.println(numberOfExperiments + " EXPERIMENTS ENDED!");
                double sumAverageTime = 0;
                for(Double averageTime: averageBeamformingTimes) {
                    System.out.println(averageTime);
                    sumAverageTime += averageTime;
                }
                double finalAverageTime = sumAverageTime / numberOfExperiments;
                System.out.println("Final average time for "
                        + SimulationService.mobileDevices.size() + " mobile devices in "
                        + numberOfExperiments + " experiments: "
                        + finalAverageTime);
                System.out.println("Max beamforming duration: " + maxBeamformingDuration);

                while (true) {
                    try {
                        sleep(300000);
                        System.out.println("Experiment Ended!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                SimulationService.stations.entrySet().forEach(this::setNewPosition);
            }
        } else {
            if(connections.isEmpty()) {
                SimulationService.stations.entrySet().forEach(this::setNewPosition);
            } else {
                SimulationService.stations.entrySet().stream()
                        .filter(entryA -> Math.random() < MOBILITY_QUOTIENT || beamformingSucceeded.get())
                        .forEach(this::setNewPosition);
            }
        }

        /*try {
            if(!firstIterationFlag) {
                sleep(4000);
            }
            firstIterationFlag = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

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
