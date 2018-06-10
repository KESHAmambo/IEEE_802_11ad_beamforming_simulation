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
public class MobileDevice extends Station {
    private static final String MOBILE_DEVICE_ID_PREFIX = "mobileDevice";
    private static final Integer DEFAULT_NUMBER_OF_SECTORS = 4;

    private static Integer mobileDeviceCount = 0;

    private volatile AccessPoint accessPoint;

    public MobileDevice(CyclicBarrier barrier, SimulationService simulationService) {
        super(MOBILE_DEVICE_ID_PREFIX + mobileDeviceCount, barrier,
                simulationService, DEFAULT_NUMBER_OF_SECTORS);
        mobileDeviceCount++;
    }

    @Override
    public void run() {
        System.out.println(getStationId() + " started");

        waitFirstBarrierAction();

        Integer count = 0;
        while(!simulationService.ended.get()) {
            System.out.println(getStationId() + ": BI " + ++count);

            waitForNewAbftPeriod();

            Connection apReceiveConnection = bestReceivedConnections.get(accessPoint.getStationId());
            Connection apTransmitConnection = bestTransmittedConnections.get(accessPoint.getStationId());
            if(positionChanged.get()) {
                //возможно, надо перенести за подтверждение
                bestTransmittedConnections.clear();
                accessPoint.bestReceivedConnections.remove(getStationId());
                positionChanged.set(false);
                System.out.println(getStationId() + " position changed, sending SSW frames to all sectors");
                sendSswFrames(true);
            } else if(apTransmitConnection == null ||
                    !apTransmitConnection.confirmed.get()) {
                System.out.println(getStationId() + " unconfirmed connection, sending SSW frames to all sectors");
                sendSswFrames(true);
            } else if(!apReceiveConnection.confirmed.get()) {
                System.out.println(getStationId() +
                        " unconfirmed AP connection, sending SSW frames to best known 'to AP' transmit sector");
                sendSswFrames(false);
            }

            awaitBarrier();
        }

        System.out.println(getStationId() + " finished");
    }

    private void sendSswFrames(Boolean toEachSector) {
        long slotNumber = chooseSlsSlot();
        long chosenSlsSlotStartTime = currentBeaconFrame.abftStartTime +
                slotNumber * SswFrame.SLS_SLOT_DURATION;
        long chosenSlsSlotEndTime = chosenSlsSlotStartTime + SswFrame.SLS_SLOT_DURATION;

        System.out.println(getStationId() + " waiting SLS slot " + slotNumber +
                ": current " + System.currentTimeMillis() +
                ", sls start " + chosenSlsSlotStartTime + ", sls end " + chosenSlsSlotEndTime);

        // Waiting for chosen SLS slot
        Utils.waitTillTime(chosenSlsSlotStartTime);

        System.out.println(getStationId() + " ended waiting SLS slot " + slotNumber);

        if(toEachSector) {
            for(int sectorNumber = 0; sectorNumber < numberOfSectors; sectorNumber++) {
                sendSswFrame(chosenSlsSlotEndTime, sectorNumber, sectorNumber + 1 == numberOfSectors);
            }
        } else {
            int sectorNumber = bestTransmittedConnections.get(
                    accessPoint.getStationId())
                    .sectorNumber;
            sendSswFrame(chosenSlsSlotEndTime, sectorNumber, true);
        }
    }

    private void sendSswFrame(long chosenSlsSlotEndTime, int sectorNumber, boolean lastSector) {
        if(Utils.enoughToTransmit(SswFrame.SIZE, chosenSlsSlotEndTime, Constants.CONTROL_PHY_SPEED)) {
            SswFrame sswFrame = new SswFrame(
                    sectorNumber, bestReceivedConnections.get(accessPoint.getStationId()),
                    getStationId(),
                    lastSector,
                    chosenSlsSlotEndTime);

            transmitting.set(true);
            accessPoint.receiving.incrementAndGet();

            Utils.waitTillTransmissionEnd(SswFrame.SIZE, Constants.CONTROL_PHY_SPEED);

            accessPoint.receiveSswFrame(sswFrame);
            transmitting.set(false);
        }
    }

    @Override
    public void receiveSswFrame(SswFrame sswFrame) {

    }

    @Override
    public void receiveSswFeedback(SswFrame sswFrame) {
        String initiatorStationId = sswFrame.initiatorStationId;
        Integer initiatorSectorNumber = sswFrame.initiatorSectorNumber;

        bestTransmittedConnections.put(initiatorStationId, sswFrame.bestReceivedConnection);
        receiving.decrementAndGet();

        sendSswAck(sswFrame, initiatorStationId);
    }

    private void sendSswAck(SswFrame sswFrame, String initiatorStationId) {
        if(Utils.enoughToTransmit(SswFrame.SIZE, sswFrame.slsSlotEndTime, Constants.CONTROL_PHY_SPEED)) {
            Connection bestTransmitConnection = sswFrame.bestReceivedConnection;

            System.out.println(getStationId() + " sends SSW ACK via: sector " +
                    bestTransmitConnection.sectorNumber);

            SswFrame sswAck = new SswFrame(
                    bestTransmittedConnections.get(initiatorStationId).sectorNumber,
                    bestReceivedConnections.get(initiatorStationId),
                    getStationId(),
                    false,
                    sswFrame.slsSlotEndTime);
            Station receivingStation = SimulationService.stations.get(initiatorStationId);

            transmitting.set(true);
            receivingStation.receiving.incrementAndGet();

            Utils.waitTillTransmissionEnd(SswFrame.SIZE, Constants.CONTROL_PHY_SPEED);

            receivingStation.receiveSswAck(sswAck);
            transmitting.set(false);

            //Confirmation for the mobile station transmit connection
            bestTransmitConnection.confirmed.set(true);
        }
    }

    @Override
    public void receiveSswAck(SswFrame sswFrame) {

    }

    private long chooseSlsSlot() {
        double rand = Math.random() * BeaconFrame.SLS_SLOTS;
        return (long) Math.floor(rand);
    }

    private void waitForNewAbftPeriod() {
        if(currentBeaconFrame != null) {
            System.out.println(getStationId() +
                    " waiting for new A-BFT, current: " + System.currentTimeMillis() +
                    ", start: " + currentBeaconFrame.abftStartTime +
                    ", Beacon end: " + currentBeaconFrame.beaconIntervalEndTime);
        }

        long currentTime = System.currentTimeMillis();
        while(currentBeaconFrame == null
                || currentBeaconFrame.abftStartTime > currentTime
                || currentBeaconFrame.beaconIntervalEndTime < currentTime) {
            try {
                sleep(1);
                currentTime = System.currentTimeMillis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(getStationId() + " ended waiting for new A-BFT, current: " + System.currentTimeMillis() +
                ", start: " + currentBeaconFrame.abftStartTime + ", Beacon end: " + currentBeaconFrame.beaconIntervalEndTime);
    }

    @Override
    public void receiveBeacon(BeaconFrame beaconFrame) {
        String initiatorStationId = beaconFrame.initiatorStationId;
        Integer initiatorSectorNumber = beaconFrame.sectorNumber;
        int beaconIntervalNumber = beaconFrame.beaconIntervalNumber;

        if(accessPoint == null) {
            accessPoint = (AccessPoint) SimulationService.stations.get(initiatorStationId);
            System.out.println(getStationId() + " found access point " + initiatorStationId);
        }

        Connection receivedConnection = BeaconIntervalBarrierAction.connections.get(initiatorStationId)
                .get(getStationId()).get(initiatorSectorNumber);
        receivedConnection.beaconIntervalNumber = beaconIntervalNumber;

        Connection bestReceivedConnectionFromInitiator = bestReceivedConnections.get(initiatorStationId);
        if(!bestReceivedConnections.containsKey(initiatorStationId)
                || bestReceivedConnectionFromInitiator.beaconIntervalNumber < beaconIntervalNumber
                || bestReceivedConnections.get(initiatorStationId).power < receivedConnection.power) {
            bestReceivedConnections.put(initiatorStationId, receivedConnection);
        }

        System.out.println(getStationId() + " received Beacon: sector " + beaconFrame.sectorNumber +
                ", power = " + receivedConnection.power);

        currentBeaconFrame = beaconFrame;
        receiving.decrementAndGet();
    }
}
