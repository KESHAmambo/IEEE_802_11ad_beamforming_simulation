from protocol.packages.package import Package
from itertools import count

PACKAGE_TYPE = 'Initiator SSW'
SIZE = 32


class InitiatorSectorSweep(Package):
  def __init__(
      self,
      sender,
      receiver,
      time,
      send_params,
      sector
  ):
    Package.__init__(self, sender, receiver, PACKAGE_TYPE, SIZE, time, send_params)
    self.sector = sector
