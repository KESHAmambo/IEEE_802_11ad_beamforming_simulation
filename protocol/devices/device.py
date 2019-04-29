from ratracer.utils import vec3d_from_array
from abc import ABCMeta, abstractmethod
from protocol.send_params import SendParams
from random import randrange
from ratracer.utils import apply_color, verbose
from inspect import isclass


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
    if not self._check_right_destination(package):
      return
    else:
      self._log_package_consume(package)
      return self._custom_consume(package)

  @verbose(color=None)
  def _log_package_consume(self, package, *, color):
    package_type = apply_color('turq')(package.package_type)
    print(self.formatted_id,
          'consumed', package_type,
          'from', package.sender.formatted_id,
          ', collisions:', len(package.collisions))

  def _check_right_destination(self, package):
    receiver = package.receiver
    return receiver == self or isclass(receiver) and isinstance(self, receiver)

  @abstractmethod
  def _custom_consume(self, package):
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

  # TODO: calculate snr properly
  def _calculate_snr(self, package):
    if len(package.collisions) > 0:
      return randrange(10, 15)
    else:
      return randrange(0, 15)
