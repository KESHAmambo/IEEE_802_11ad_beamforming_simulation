from itertools import count
from random import randrange

from protocol.devices.device import Device
from protocol.packages.beacon import Beacon
from protocol.packages.responder_sector_sweep import ResponderSectorSweep, SIZE as RESPONDER_SSW_SIZE
from protocol.packages.ssw_ack import SswAck
from protocol.packages.ssw_feedback import SswFeedback
from protocol.protocol_params import CONTROL_PHY_SPEED

DEVICE_TYPE = 'Mobile'
COLOR = 'blue'


class Mobile(Device):
  _id_count = count(0)

  def __init__(self, coords, config):
    Device.__init__(self, DEVICE_TYPE, next(self._id_count), COLOR, coords, config)
    self.access_point = None
    self.a_bft_start = None
    self._best_initiator_sector = None
    self._best_initiator_snr = None
    self._prev_best_initiator_sector = None
    self._prev_best_initiator_snr = None
    self.best_send_sector = None

  def shift_position(self, shift_coords):
    new_coords = []

    for i in range(3):
      new_coords[i] = self.coords[i] + shift_coords[i]

    self.coords = new_coords
    self.update_position(self.coords)

  def _custom_consume(self, package, snr):
    if isinstance(package, Beacon):
      return self._process_beacon(package, snr)
    elif isinstance(package, SswFeedback):
      return self._process_ssw_feedback(package)

  def _custom_reject(self, package):
    if isinstance(package, Beacon):
      return self._send_sector_sweeps(package)

  def _process_ssw_feedback(self, package):
    self.best_send_sector = package.best_responder_sector

    send_params = self._get_send_params(self.best_send_sector, CONTROL_PHY_SPEED)

    ssw_feedback = SswAck(
      self,
      package.sender,
      package.end_time,
      send_params
    )

    return [ssw_feedback]

  def _process_beacon(self, package, snr):
    if self.best_send_sector is not None \
        and not self._is_connected(package):
      self.best_send_sector = None
      self._prev_best_initiator_snr = None
      self._prev_best_initiator_sector = None

    if self._best_initiator_sector is None or snr > self._best_initiator_snr:
      self._best_initiator_snr = snr
      self._best_initiator_sector = package.send_params.sector

    return self._send_sector_sweeps(package)

  def _is_connected(self, beacon):
    connection = beacon.connections.get(self.id)
    return connection is not None and connection.is_ack

  def _send_sector_sweeps(self, beacon):
    if beacon.send_params.sector == beacon.sender.config.sectors - 1 \
        and self._best_initiator_sector is not None \
        and self.best_send_sector is None:
      sls_slot = randrange(0, beacon.sls_slots)
      sls_slot_start = beacon.a_bft_start + sls_slot * beacon.sls_slot_duration

      sector_sweeps = []

      for sector in range(self.config.sectors):
        send_params = self._get_send_params(sector, CONTROL_PHY_SPEED)
        sector_sweep = ResponderSectorSweep(
          self,
          beacon.sender,
          sls_slot_start + sector * RESPONDER_SSW_SIZE / CONTROL_PHY_SPEED,
          send_params,
          sector,
          self._best_initiator_sector,
          self._best_initiator_snr,
          sls_slot
        )
        sector_sweeps.append(sector_sweep)

      self._prev_best_initiator_sector = self._best_initiator_sector
      self._prev_best_initiator_snr = self._best_initiator_snr
      self._best_initiator_sector = None
      self._best_initiator_snr = None

      return sector_sweeps

  def _get_antenna_params(self, sector):
    return {"sector": sector}  # TODO: return antenna params based on antenna configuration for this sector
