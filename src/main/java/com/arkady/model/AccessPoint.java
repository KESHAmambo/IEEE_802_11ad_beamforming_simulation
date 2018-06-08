package com.arkady.model;

import com.arkady.simulation.SimulationService;
import com.arkady.model.frames.BeaconFrame;
import com.arkady.model.frames.SswFrame;
import com.arkady.utils.BeaconIntervalBarrierAction;
import com.arkady.utils.Constants;
import com.arkady.utils.Utils;

import java.util.concurrent.CyclicBarrier;

/**
 * Created by abara on 13.05.2018.
 */
public class AccessPoint extends Station {
    private static final String ACCESS_POINT_ID_PREFIX = "accessPoint";
    private static final Integer DEFAULT_NUMBER_OF_SECTORS = 8;

    private static Integer accessPointCount = 0;

    public AccessPoint(CyclicBarrier barrier, SimulationService simulationService) {
        super(ACCESS_POINT_ID_PREFIX + accessPointCount, barrier,
                simulationService, DEFAULT_NUMBER_OF_SECTORS);
        accessPointCount++;
    }

    @Override
    public void run() {
        System.out.println(getStationId() + " started");

        waitFirstBarrierAction();

        Integer beaconIntervalsCount = 0;
        while(!simulationService.ended.get()) {
            beaconIntervalsCount = sendBeacons(beaconIntervalsCount);

            awaitBarrier();
        }

        System.out.println(getStationId() + " finished");
    }

    private Integer sendBeacons(Integer beaconIntervalsCount) {
        System.out.println(getStationId() + ": BI " + ++beaconIntervalsCount);

        long beaconIntervalStartTime = System.currentTimeMillis();
        long beaconIntervalEndTime = beaconIntervalStartTime + BeaconFrame.BEACON_INTERVAL_DURATION;
        long abftStartTime = beaconIntervalStartTime + BeaconFrame.BTI_DURATION;
        long atiStartTime = abftStartTime + BeaconFrame.ABFT_DURATION;
        long dtiStartTime = atiStartTime + BeaconFrame.ATI_DURATION;

        for(int sectorNumber = 0; sectorNumber < numberOfSectors; sectorNumber++) {
            if(Utils.enoughToTransmit(BeaconFrame.SIZE, abftStartTime, Constants.CONTROL_PHY_SPEED)) {
                currentBeaconFrame = new BeaconFrame(
                        beaconIntervalStartTime,
                        beaconIntervalEndTime,
                        abftStartTime,
                        atiStartTime,
                        dtiStartTime,
                        getStationId(),
                        sectorNumber);

                SimulationService.stations.entrySet().stream().filter(entry -> !entry.getKey().equals(getStationId())).forEach(entry -> {
                    Station station = entry.getValue();
                    transmitting.set(true);
                    station.receiving.set(true);
                });

                Utils.waitTillTransmissionEnd(BeaconFrame.SIZE, Constants.CONTROL_PHY_SPEED);

                SimulationService.stations.entrySet().stream().filter(entry -> !entry.getKey().equals(getStationId())).forEach(entry -> {
                    Station station = entry.getValue();
                    station.receiveBeacon(currentBeaconFrame);
                    transmitting.set(false);
                });
            }
        }
        return beaconIntervalsCount;
    }

    @Override
    public void receiveSswFrame(SswFrame sswFrame) {
        String initiatorStationId = sswFrame.initiatorStationId;
        Integer initiatorSectorNumber = sswFrame.initiatorSectorNumber;

        bestTransmittedConnections.put(initiatorStationId, sswFrame.bestReceivedConnection);

        Connection receivedConnection = BeaconIntervalBarrierAction.connections.get(initiatorStationId)
                .get(getStationId()).get(initiatorSectorNumber);

        if(!bestReceivedConnections.containsKey(initiatorStationId)
                || bestReceivedConnections.get(initiatorStationId).power < receivedConnection.power) {
            bestReceivedConnections.put(initiatorStationId, receivedConnection);
        }
        receiving.set(false);

        System.out.println(getStationId() + " received SSW frame: initiator sector " + initiatorSectorNumber +
                ", power = " + receivedConnection.power);

        if(sswFrame.lastSector) {
            sendSswFeedback(sswFrame, initiatorStationId);
        }
    }

    private void sendSswFeedback(SswFrame sswFrame, String initiatorStationId) {
        if(Utils.enoughToTransmit(SswFrame.SIZE, sswFrame.slsSlotEndTime, Constants.CONTROL_PHY_SPEED)) {
            System.out.println(getStationId() + " sends SSW Feedback via: sector " + bestTransmittedConnections.get(initiatorStationId).sectorNumber);

            SswFrame sswFeedback = new SswFrame(
                    bestTransmittedConnections.get(initiatorStationId).sectorNumber,
                    bestReceivedConnections.get(initiatorStationId),
                    getStationId(),
                    false,
                    sswFrame.slsSlotEndTime);
            Station receivingStation = SimulationService.stations.get(initiatorStationId);

            transmitting.set(true);
            receivingStation.receiving.set(true);

            Utils.waitTillTransmissionEnd(SswFrame.SIZE, Constants.CONTROL_PHY_SPEED);

            receivingStation.receiveSswFeedback(sswFeedback);
            transmitting.set(false);

            //Confirmation for the AP transmit connection
            bestTransmittedConnections.get(initiatorStationId).confirmed.set(true);
        }
    }

    @Override
    public void receiveBeacon(BeaconFrame beaconFrame) {

    }

    @Override
    public void receiveSswFeedback(SswFrame sswFrame) {

    }

    @Override
    public void receiveSswAck(SswFrame sswFrame) {
        // TODO: 01.06.2018 ???

        String initiatorStationId = sswFrame.initiatorStationId;
        Integer initiatorSectorNumber = sswFrame.initiatorSectorNumber;

        receiving.set(false);
    }
}
