from protocol.devices.device import Device
from itertools import count
from protocol.packages.beacon import Beacon, SIZE as BEACON_SIZE
from protocol.packages.ssw_feedback import SswFeedback, SIZE as SSW_FEEDBACK_SIZE
from protocol.packages.ssw_ack import SswAck
from protocol.protocol_params import CONTROL_PHY_SPEED
from protocol.packages.responder_sector_sweep import ResponderSectorSweep
from protocol.devices.mobile import Mobile

DEVICE_TYPE = 'Access point'
COLOR = 'green'


class Connection:
  def __init__(self, best_receive_sector, snr, best_send_sector):
    self.best_receive_sector = best_receive_sector
    self.snr = snr
    self.best_send_sector = best_send_sector
    self.is_ack = False


class AccessPoint(Device):
  _id_count = count(0)

  def __init__(self, coords, config):
    Device.__init__(self, DEVICE_TYPE, next(self._id_count), COLOR, coords, config)
    self.connections = dict()

  def send_beacons(self, current_time):
    self._clear_not_acked_connections()

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
        self.config.sls_slot_duration,
        self.connections
      )
      beacons.append(beacon)

    return beacons

  def _clear_not_acked_connections(self):
    not_acked_connections = set()

    for device_id, connection in self.connections.items():
      if not connection.is_ack:
        not_acked_connections.add(device_id)

    for device_id in not_acked_connections:
      self.connections.pop(device_id)

  def _get_antenna_params(self, sector):
    return {"sector": sector}  # TODO: return antenna params based on antenna configuration for this sector

  def _custom_consume(self, package, snr):
    if isinstance(package, ResponderSectorSweep):
      return self._process_responder_sector_sweep(package, snr)
    elif isinstance(package, SswAck):
      return self._process_ssw_ack(package)

  def _custom_reject(self, package):
    if isinstance(package, ResponderSectorSweep):
      return self._send_ssw_feedback(package)

  def _process_ssw_ack(self, package):
    connection = self.connections.get(package.sender.id)
    connection.is_ack = True
    connection.time = package.end_time

  def _process_responder_sector_sweep(self, package, snr):
    prev_connection = self.connections.get(package.sender.id)

    if prev_connection is None or snr > prev_connection.snr:
      connection = Connection(package.send_params.sector, snr, package.best_initiator_sector)
      self.connections[package.sender.id] = connection

    return self._send_ssw_feedback(package)

  def _send_ssw_feedback(self, package):
    connection = self.connections.get(package.sender.id)

    if package.sector == package.sender.config.sectors - 1 \
        and connection is not None:

      send_params = self._get_send_params(connection.best_send_sector, CONTROL_PHY_SPEED)

      ssw_feedback = SswFeedback(
        self,
        package.sender,
        package.end_time,
        send_params,
        connection.best_receive_sector,
        connection.snr
      )

      return [ssw_feedback]

  def is_all_devices_connected(self, devices):
    for device in devices:
      connection = self.connections.get(device.id)
      if connection is None or not connection.is_ack:
        return False

    return True


