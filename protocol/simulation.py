from protocol.package_queue import PackageQueue
from protocol.device_heap import DeviceHeap
from protocol.devices.access_point import AccessPoint
from protocol.devices.mobile import Mobile
from protocol.devices.access_point_config import AccessPointConfig
from protocol.devices.mobile_config import MobileConfig
from ratracer.utils import apply_color

ITERATIONS = 5

initial_access_pints_coords = [
  [0, 0, 0]
]
initial_mobile_coords = [
  [0, 10, 10],
  [0, 10, 30]
]

package_queue = PackageQueue()
device_heap = DeviceHeap()

for coords in initial_access_pints_coords:
  access_point = AccessPoint(coords, AccessPointConfig())
  device_heap.add_access_point(access_point)

for coords in initial_mobile_coords:
  mobile = Mobile(coords, MobileConfig())
  device_heap.add_mobile(mobile)

current_time = 0
next_beacon_time = 0

for i in range(ITERATIONS):
  print("Iteration", i)

  current_time = next_beacon_time

  for access_point in device_heap.access_points:
    beacons = access_point.send_beacons(current_time)
    next_beacon_time = beacons[0].next_beacon_time
    package_queue.update(beacons)

  while package_queue.length() > 0:
    package = package_queue.get_next()
    current_time = package.time

    print('Processing package', apply_color('turq')(package.package_type))
    package_queue.check_collisions(package)

    for device in device_heap.devices:
      new_packages = device.consume(package)
      if new_packages is not None:
        package_queue.update(new_packages)


