from ratracer.utils import vec3d_from_array


class DeviceHeap:
  def __init__(self):
    self.devices = set()
    self.access_points = set()
    self.mobiles = set()

  def add_access_point(self, access_point):
    self.devices.add(access_point)
    self.access_points.add(access_point)

  def add_mobile(self, mobile):
    self.devices.add(mobile)
    self.mobiles.add(mobile)