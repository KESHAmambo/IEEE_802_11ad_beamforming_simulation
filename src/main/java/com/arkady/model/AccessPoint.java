package com.arkady.model;

import com.arkady.simulation.SimulationService;
import com.arkady.model.frames.BeaconFrame;
import com.arkady.model.frames.SswFrame;
import com.arkady.utils.BeaconIntervalBarrierAction;
import com.arkady.utils.Constants;
import com.arkady.utils.Utils;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by abara on 13.05.2018.
 */
public class AccessPoint extends Station {
    private static final String ACCESS_POINT_ID_PREFIX = "accessPoint";
    private static final Integer DEFAULT_NUMBER_OF_SECTORS = 8;

    private static Integer accessPointCount = 0;

    private volatile long currentSlsEndTime;
    private volatile String currentSlsPairingStationId;
    private volatile AtomicBoolean sswFeedbackWasAlreadySent = new AtomicBoolean(true);

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

            // Send ssw feedbacks in each SLS slot where at least one SSW frame was accepted
            sendSswFeedbacks();

            Utils.waitTillTime(currentBeaconFrame.beaconIntervalEndTime);

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
                        sectorNumber,
                        beaconIntervalsCount);

                int finalSectorNumber = sectorNumber;
                SimulationService.stations.entrySet().stream().filter(entry -> !entry.getKey().equals(getStationId())).forEach(entry -> {
                    Station station = entry.getValue();
                    transmitting.set(true);
//                    station.receiving.incrementAndGet();
                    Connection transmitConnection = BeaconIntervalBarrierAction.connections.get(getStationId())
                            .get(station.getStationId()).get(finalSectorNumber);
                    CollisionPretender collisionPretender = new CollisionPretender(transmitConnection);
                    station.collisionPretenders.put(getStationId(), collisionPretender);
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

    private void sendSswFeedbacks() {
        for(int slotNumber = 0; slotNumber < BeaconFrame.SLS_SLOTS; slotNumber++) {
            long sswFeedbackTime = currentBeaconFrame.abftStartTime
                    + SswFrame.SLS_SLOT_DURATION * slotNumber
                    + SswFrame.SSW_FEEDBACK_TIME_IN_SLS_SLOT;
            Utils.waitTillTime(sswFeedbackTime);

            if(!sswFeedbackWasAlreadySent.get()) {
                sendSswFeedback(currentSlsEndTime, currentSlsPairingStationId);
            }
        }
    }

    @Override
    public void receiveSswFrame(SswFrame sswFrame) {
        String initiatorStationId = sswFrame.initiatorStationId;
        Integer initiatorSectorNumber = sswFrame.initiatorSectorNumber;

        boolean snrAcceptable = Utils.isSnrAcceptable(initiatorStationId, collisionPretenders);

        if(snrAcceptable) {
            bestTransmittedConnections.put(initiatorStationId, sswFrame.bestReceivedConnection);

            Connection receivedConnection = BeaconIntervalBarrierAction.connections.get(initiatorStationId)
                    .get(getStationId()).get(initiatorSectorNumber);

            if(!bestReceivedConnections.containsKey(initiatorStationId)
                    || bestReceivedConnections.get(initiatorStationId).power < receivedConnection.power) {
                bestReceivedConnections.put(initiatorStationId, receivedConnection);
            }
//            receiving.decrementAndGet();

            System.out.println(getStationId() +
                    " received SSW frame: initiator sector " + initiatorSectorNumber +
                    ", power = " + receivedConnection.power);

            currentSlsPairingStationId = initiatorStationId;
            currentSlsEndTime = sswFrame.slsSlotEndTime;
            sswFeedbackWasAlreadySent.set(false);
            /*
            if(sswFrame.lastSector) {
                sendSswFeedback(sswFrame, initiatorStationId);
            }*/
        } else {
            System.out.println(getStationId() + " unacceptable SNR: " +
                    initiatorStationId +
                    " SSW frame from sector " + initiatorSectorNumber);
        }

        Utils.clearCollisionPretendersIfChecked(collisionPretenders);
    }

    private void sendSswFeedback(long slsSlotEndTime, String receivingStationId) {
        if(Utils.enoughToTransmit(SswFrame.SIZE, slsSlotEndTime, Constants.CONTROL_PHY_SPEED)) {
            System.out.println(getStationId() + " sends SSW Feedback via: sector " + bestTransmittedConnections.get(receivingStationId).sectorNumber);

            SswFrame sswFeedback = new SswFrame(
                    bestTransmittedConnections.get(receivingStationId).sectorNumber,
                    bestReceivedConnections.get(receivingStationId),
                    getStationId(),
                    false,
                    slsSlotEndTime);
            Station receivingStation = SimulationService.stations.get(receivingStationId);

            transmitting.set(true);
            receivingStation.receiving.incrementAndGet();

            Utils.waitTillTransmissionEnd(SswFrame.SIZE, Constants.CONTROL_PHY_SPEED);

            receivingStation.receiveSswFeedback(sswFeedback);
            transmitting.set(false);

            //Confirmation for the AP transmit connection
            bestTransmittedConnections.get(receivingStationId).confirmed.set(true);
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

        receiving.decrementAndGet();
    }
}
