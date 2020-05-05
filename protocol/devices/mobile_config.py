from protocol.devices.device_config import DeviceConfig


class MobileConfig(DeviceConfig):
  def __init__(self):
    DeviceConfig.__init__(self)
    self.sectors = 8
