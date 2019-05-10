from ratracer.utils import apply_color, verbose_uncolored


class Package:
  def __init__(self, sender, receiver, package_type, size, time, send_params):
    self.sender = sender
    self.receiver = receiver
    self.time = time
    self.end_time = time + size / send_params.speed
    self.package_type = package_type
    self.size = size
    self.send_params = send_params
    self.collisions = set()

  @verbose_uncolored
  def print(self):
    print('[package]', apply_color('turq')(self.package_type),
          'from', self.sender.formatted_id,
          ', time', self.time,
          ', collisions:', len(self.collisions))
    self.custom_print()

  @verbose_uncolored
  def custom_print(self): pass
