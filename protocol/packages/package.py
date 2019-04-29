class Package:
  def __init__(self, sender, receiver, package_type, size, time, send_params):
    self.sender = sender
    self.receiver = receiver
    self.time = time
    self.package_type = package_type
    self.size = size
    self.send_params = send_params
    self.collisions = set()
