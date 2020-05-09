from protocol.devices.device_config import DeviceConfig
from protocol.packages.beacon import SIZE as BEACON_SIZE
from protocol.protocol_params import CONTROL_PHY_SPEED, SLS_SLOT_DURATION


class AccessPointConfig(DeviceConfig):
  def __init__(self, sls_slots, sls_dynamic_mode, sls_degree_adjust):
    DeviceConfig.__init__(self)
    self.sectors = 12
    self.beacon_interval = 102.4
    self.a_bft_start = self.sectors * BEACON_SIZE / CONTROL_PHY_SPEED
    self.sls_slots = sls_slots
    self.sls_dynamic_mode = sls_dynamic_mode
    self.sls_slots_base = sls_slots
    self.sls_degree_adjust = sls_degree_adjust

    self.sls_slot_duration = SLS_SLOT_DURATION
