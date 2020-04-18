import numpy
import math
from ratracer.utils import vec3d_from_array
from abc import abstractmethod
from protocol.send_params import SendParams
from random import randrange
from ratracer.utils import apply_color, verbose_uncolored
from inspect import isclass
from protocol.protocol_params import MIN_SNR, CONTROL_PHY_FREQ, NOISE_POWER, REFLECTIONS
from ratracer.radio import AntennaPattern, RFDevice, to_log, power
from ratracer.utils import vec3d
from protocol.pathloss_model import PATHLOSS_MODEL

def color_snr(snr):
  color = None

  if snr < MIN_SNR:
    color = 'red'
  else:
    color = 'green'

  return apply_color(color)(snr)


def get_ant_gain_based_on_sectors(sectors):
  if sectors == 4:
    return 5.5
  if sectors == 6:
    return 8.5
  if sectors == 8:
    return 12.0
  if sectors == 10:
    return 12.5
  if sectors == 12:
    return 14.0
  if sectors == 24:
    return 20.5


RX_ANT_PATTERN = AntennaPattern(kind='isotropic')


class Device:
  def __init__(self, device_type, type_counter, color, coords, config):
    self.type = device_type
    self.id = device_type + ' ' + repr(type_counter)
    self.formatted_id = apply_color(color)(self.id)
    self.coords = coords
    self.config = config
    self.ant_gain = get_ant_gain_based_on_sectors(config.sectors)
    self.update_position(coords)
    self._rf_device = RFDevice(self.position, CONTROL_PHY_FREQ, pattern=RX_ANT_PATTERN)


  def update_position(self, coords):
    self.position = vec3d_from_array(coords)

  def consume(self, package):
    snr = self._calculate_snr(package)
    if not self._check_right_destination(package):
      return
    elif snr < MIN_SNR:
      self._log_package_reject(package, snr)
      return self._custom_reject(package)
    else:
      self._log_package_consume(package, snr)
      return self._custom_consume(package, snr)

  @verbose_uncolored
  def _log_package_consume(self, package, snr):
    print(self.formatted_id, 'consumed',
          ', snr', color_snr(snr))

  @verbose_uncolored
  def _log_package_reject(self, package, snr):
    print(self.formatted_id, 'rejected',
          ', snr', color_snr(snr))

  def _check_right_destination(self, package):
    receiver = package.receiver
    return receiver == self or isclass(receiver) and isinstance(self, receiver)

  @abstractmethod
  def _custom_reject(self, package):
    raise NotImplementedError

  @abstractmethod
  def _custom_consume(self, package, snr):
    raise NotImplementedError

  @abstractmethod
  def _get_antenna_params(self, sector):
    raise NotImplementedError

  def _get_send_params(self, sector, speed):
    antenna_params = self._get_antenna_params(sector)
    return SendParams(
      self.position,
      antenna_params,
      sector,
      speed
    )

  def _calculate_snr(self, package):
    income_power_mW = self._calculate_income_power(package)

    income_collisions_power_mW = 0
    for collided_package in package.collisions:
      income_collisions_power_mW += self._calculate_income_power(collided_package)

    return income_power_mW / (NOISE_POWER + income_collisions_power_mW)

  def _calculate_income_power(self, package):
    emit_sector = package.send_params.sector

    tx = package.sender
    sectors_tx = tx.config.sectors
    coords_tx = tx.position

    scene = {
      # (0, 0, 0): (0, 0, 1),
      # (0, 0, 10): (0, 0, -1),
      # (5, 0, 0): (-1, 0, 0),
      # (-5, 0, 0): (1, 0, 0),
    }

    sector_width = numpy.pi * 2 / sectors_tx
    tx_ant_pattern = AntennaPattern(kind='sector', sector_width=sector_width)

    # TODO: calc transmitter ant_normal based on emit_sector and station rotation
    tx_ant_rot = emit_sector * sector_width
    tx_ant_normal = vec3d(numpy.cos(tx_ant_rot), numpy.sin(tx_ant_rot), 0)

    transmitter = RFDevice(coords_tx, CONTROL_PHY_FREQ, pattern=tx_ant_pattern, ant_normal=tx_ant_normal)

    pathloss = PATHLOSS_MODEL(transmitter, self._rf_device, max_reflections=REFLECTIONS)
    log_pathloss = to_log(power(pathloss))
    income_power_dBm = 30. + log_pathloss + tx.ant_gain
    income_power_mW = numpy.power(10, income_power_dBm / 10)

    return income_power_mW
