from protocol.devices.device_config import DeviceConfig
from protocol.packages.beacon import SIZE as BEACON_SIZE
from protocol.protocol_params import CONTROL_PHY_SPEED, MAX_SECTORS_FOR_MOBILE
from protocol.packages.initiator_sector_sweep import SIZE as INITIATOR_SSW_SIZE
from protocol.packages.responder_sector_sweep import SIZE as RESPONDER_SSW_SIZE
from protocol.packages.sector_sweep_feedback import SIZE as SSW_FEEDBACK_SIZE
from protocol.packages.sector_sweep_ack import SIZE as SSW_ACK_SIZE


class AccessPointConfig(DeviceConfig):
  def __init__(self):
    DeviceConfig.__init__(self)
    self.sectors = 12
    self.beacon_interval = 102.4
    self.a_bft_start = self.sectors * BEACON_SIZE / CONTROL_PHY_SPEED
    self.sls_slots = 3

    sls_slot_data_size = MAX_SECTORS_FOR_MOBILE * RESPONDER_SSW_SIZE \
                           + SSW_FEEDBACK_SIZE \
                           + SSW_ACK_SIZE
    self.sls_slot_duration = sls_slot_data_size / CONTROL_PHY_SPEED
