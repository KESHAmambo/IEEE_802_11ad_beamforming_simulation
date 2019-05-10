from protocol.packages.package import Package

PACKAGE_TYPE = 'SSW Ack'
SIZE = 16


class SswAck(Package):
  def __init__(
      self,
      sender,
      receiver,
      time,
      send_params
  ):
    Package.__init__(self, sender, receiver, PACKAGE_TYPE, SIZE, time, send_params)
