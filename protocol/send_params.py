class SendParams:
  def __init__(self, from_position, antenna_params, sector, speed):
    self.from_position = from_position
    self.antenna_pattern = antenna_params
    self.sector = sector
    self.speed = speed
