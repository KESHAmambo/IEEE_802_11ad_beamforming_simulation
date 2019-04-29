from protocol.packages.package import Package
from itertools import count

PACKAGE_TYPE = 'Beacon'
SIZE = 512


class Beacon(Package):
  def __init__(
      self,
      sender,
      receiver,
      time,
      send_params,
      next_beacon_time,
      a_bft_start,
      sls_slots,
      sls_slot_duration
  ):
    Package.__init__(self, sender, receiver, PACKAGE_TYPE, SIZE, time, send_params)
    self.next_beacon_time = next_beacon_time
    self.a_bft_start = a_bft_start
    self.sls_slots = sls_slots
    self.sls_slot_duration = sls_slot_duration
