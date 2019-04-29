from protocol.devices.device import Device
from itertools import count
from protocol.packages.beacon import Beacon, SIZE as BEACON_SIZE
from protocol.protocol_params import CONTROL_PHY_SPEED
from protocol.packages.responder_sector_sweep import ResponderSectorSweep
from protocol.devices.mobile import Mobile

DEVICE_TYPE = 'Access point'
COLOR = 'green'

class AccessPoint(Device):
  _id_count = count(0)

  def __init__(self, coords, config):
    Device.__init__(self, DEVICE_TYPE, next(self._id_count), COLOR, coords, config)

  def send_beacons(self, current_time):
    beacons = []

    for sector in range(self.config.sectors):
      antenna_params = self._get_antenna_params(sector)
      send_params = self._get_send_params(sector, CONTROL_PHY_SPEED)
      beacon = Beacon(
        self,
        Mobile,
        current_time + sector * BEACON_SIZE / CONTROL_PHY_SPEED,
        send_params,
        current_time + self.config.beacon_interval,
        current_time + self.config.a_bft_start,
        self.config.sls_slots,
        self.config.sls_slot_duration
      )
      beacons.append(beacon)

    return beacons

  def _get_antenna_params(self, sector):
    return {"sector": sector}  # TODO: return antenna params based on antenna configuration for this sector

  def _custom_consume(self, package):
    if isinstance(package, ResponderSectorSweep):
      return  # TODO: implement producing SSW Feedback
