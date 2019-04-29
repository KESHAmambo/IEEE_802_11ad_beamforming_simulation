from protocol.packages.package import Package
from itertools import count

PACKAGE_TYPE = 'SSW Feedback'
SIZE = 64


class SectorSweepFeedback(Package):
  def __init__(
      self,
      sender,
      receiver,
      time,
      send_params,
      best_initiator_sector,
      best_initiator_snr
  ):
    Package.__init__(self, sender, receiver, PACKAGE_TYPE, SIZE, time, send_params)
    self.best_initiator_sector = best_initiator_sector
    self.best_initiator_snr = best_initiator_snr
