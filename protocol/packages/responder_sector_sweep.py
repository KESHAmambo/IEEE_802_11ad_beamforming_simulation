from protocol.packages.package import Package
from ratracer.utils import verbose_uncolored

PACKAGE_TYPE = 'Responder SSW'
SIZE = 64


class ResponderSectorSweep(Package):
  def __init__(
      self,
      sender,
      receiver,
      time,
      send_params,
      sector,
      best_initiator_sector,
      best_initiator_snr,
      sls_slot
  ):
    Package.__init__(self, sender, receiver, PACKAGE_TYPE, SIZE, time, send_params)
    self.sector = sector
    self.best_initiator_sector = best_initiator_sector
    self.best_initiator_snr = best_initiator_snr
    self.sls_slot = sls_slot

  @verbose_uncolored
  def custom_print(self):
    print('sls_slot', self.sls_slot)