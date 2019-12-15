from ratracer.utils import vec3d_from_array
from abc import abstractmethod
from protocol.send_params import SendParams
from random import randrange
from ratracer.utils import apply_color, verbose_uncolored
from inspect import isclass
from protocol.protocol_params import MIN_SNR


def color_snr(snr):
  color = None

  if snr < MIN_SNR:
    color = 'red'
  else:
    color = 'green'

  return apply_color(color)(snr)


class Device:
  def __init__(self, device_type, type_counter, color, coords, config):
    self.type = device_type
    self.id = device_type + ' ' + repr(type_counter)
    self.formatted_id = apply_color(color)(self.id)
    self.coords = coords
    self.config = config
    self.update_position(coords)

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

  # TODO: calculate snr properly via ../../ratracer/radio.py module
  def _calculate_snr(self, package):
    if len(package.collisions) > 0:
      return randrange(0, 6)
    else:
      return randrange(6, 30)
