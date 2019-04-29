from protocol.devices.device import Device
from itertools import count
from protocol.packages.beacon import Beacon
from protocol.packages.sector_sweep_feedback import SectorSweepFeedback
from random import randrange
from protocol.packages.responder_sector_sweep import ResponderSectorSweep, SIZE as RESPONDER_SSW_SIZE
from protocol.protocol_params import CONTROL_PHY_SPEED

DEVICE_TYPE = 'Mobile'
COLOR = 'blue'


class Mobile(Device):
  _id_count = count(0)

  def __init__(self, coords, config):
    Device.__init__(self, DEVICE_TYPE, next(self._id_count), COLOR, coords, config)
    self.access_point = None
    self.a_bft_start = None
    self._beacons_received = None
    self._best_initiator_sector = None
    self._best_initiator_snr = None

  def shift_position(self, shift_coords):
    new_coords = []

    for i in range(3):
      new_coords[i] = self.coords[i] + shift_coords[i]

    self.coords = new_coords
    self.update_position(self.coords)

  def _custom_consume(self, package):
    if isinstance(package, Beacon):
      return self.produce_responder_sector_sweeps(package)
    elif isinstance(package, SectorSweepFeedback):
      print('TODO', package.package_type)

  def produce_responder_sector_sweeps(self, beacon):
    if beacon.a_bft_start != self.a_bft_start:
      self.a_bft_start = beacon.a_bft_start
      self._beacons_received = 1
      self._best_initiator_sector = beacon.send_params.sector
      self._best_initiator_snr = self._calculate_snr(beacon)
    else:
      self._beacons_received += 1
      snr = self._calculate_snr(beacon)
      if snr > self._best_initiator_snr:
        self._best_initiator_snr = snr
        self._best_initiator_sector = beacon.send_params.sector

    if self._beacons_received == beacon.sender.config.sectors:
      sls_slot = randrange(0, beacon.sls_slots)
      sls_slot_start = beacon.a_bft_start + sls_slot * beacon.sls_slot_duration

      sector_sweeps = []

      for sector in range(self.config.sectors):
        send_params = self._get_send_params(sector, CONTROL_PHY_SPEED)
        sector_sweep = ResponderSectorSweep(
          self,
          beacon.sender,
          sls_slot_start + RESPONDER_SSW_SIZE / CONTROL_PHY_SPEED,
          send_params,
          sector,
          self._best_initiator_sector,
          self._best_initiator_snr
        )
        sector_sweeps.append(sector_sweep)

      return sector_sweeps

  def _get_antenna_params(self, sector):
    return {"sector": sector}  # TODO: return antenna params based on antenna configuration for this sector
