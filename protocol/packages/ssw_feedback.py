from protocol.packages.package import Package
from itertools import count

PACKAGE_TYPE = 'SSW Feedback'
SIZE = 64


class SswFeedback(Package):
  def __init__(
      self,
      sender,
      receiver,
      time,
      send_params,
      best_responder_sector,
      best_responder_snr
  ):
    Package.__init__(self, sender, receiver, PACKAGE_TYPE, SIZE, time, send_params)
    self.best_responder_sector = best_responder_sector
    self.best_responder_snr = best_responder_snr
