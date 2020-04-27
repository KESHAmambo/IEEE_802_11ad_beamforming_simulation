from protocol.device_heap import DeviceHeap
from protocol.devices.access_point import AccessPoint
from protocol.devices.access_point_config import AccessPointConfig
from protocol.devices.mobile import Mobile
from protocol.devices.mobile_config import MobileConfig
from protocol.package_queue import PackageQueue
from ratracer.utils import verbose_uncolored
from protocol.pathloss_model import CARRIAGE_HALF_WIDTH
from random import randrange

ZONE_LENGTH = 4.56

DEVICE_MARGIN = 0.05
PADDING = 0.05
MIN_X_COORD = -ZONE_LENGTH
MAX_X_COORD = 0
MIN_Y_COORD = -CARRIAGE_HALF_WIDTH + PADDING
MAX_Y_COORD = CARRIAGE_HALF_WIDTH - PADDING

ITERATIONS = 100

access_point_config = AccessPointConfig()
mobile_config = MobileConfig()

initial_access_pint_coords = [0., 0., 0.]

# number_of_mobile_stations = 1
number_of_mobile_stations = 2
# number_of_mobile_stations = 4
# number_of_mobile_stations = 6
# number_of_mobile_stations = 10
# number_of_mobile_stations = 14
# number_of_mobile_stations = 18
# number_of_mobile_stations = 20
# number_of_mobile_stations = 22
# number_of_mobile_stations = 24
# number_of_mobile_stations = 25

initial_mobile_coords = [
  # [1., 0., 0.],
  # [2., 0., 0.],
  # [3., 0., 0.],
  # [4., 0., 0.],
  # [5., 0., 0.],
  # [6., 0., 0.],
  #
  # [-1., 0., 0.],
  # [-2., 0., 0.],
  # [-3., 0., 0.],
  # [-4., 0., 0.],
  # [-5., 0., 0.],
  # [-6., 0., 0.],
  #
  # [0., 1., 0.],
  # [0., 2., 0.],
  # [0., 3., 0.],
  # [0., 4., 0.],
  # [0., 5., 0.],
  # [0., 6., 0.],
  #
  # [0., -1., 0.],
  # [0., -2., 0.],
  # [0., -3., 0.],
  # [0., -4., 0.],
  # [0., -5., 0.],
  # [0., -6., 0.],
]

LENGTH = -MIN_X_COORD + MAX_X_COORD
WIDTH = -MIN_Y_COORD + MAX_Y_COORD
MARGINS_IN_LENGTH = LENGTH // DEVICE_MARGIN
MARGINS_IN_WIDTH = WIDTH // DEVICE_MARGIN

for i in range(number_of_mobile_stations):
  x = DEVICE_MARGIN * randrange(0, MARGINS_IN_LENGTH) + MIN_X_COORD
  y = DEVICE_MARGIN * randrange(0, MARGINS_IN_WIDTH) + MIN_Y_COORD
  initial_mobile_coords.append([x, y, 0])


def calc_average_connection_time(connections):
  sum_time = 0
  for device_id, connection in connections.items():
    sum_time += connection.time

  return sum_time / len(connections)


@verbose_uncolored
def log_iteration(iteration):
  print("\nIteration", iteration)


def run_simulation(simulation_count):
  print('--- Simulation run --- (', simulation_count, '/', ITERATIONS, ')')

  package_queue = PackageQueue()
  device_heap = DeviceHeap()

  access_point = AccessPoint(initial_access_pint_coords, access_point_config)
  device_heap.add_access_point(access_point)

  for coords in initial_mobile_coords:
    mobile = Mobile(coords, mobile_config)
    device_heap.add_mobile(mobile)

  current_time = 0
  next_beacon_time = 0

  iter_counter = 0

  while not access_point.is_all_devices_connected(device_heap.mobiles):
    iter_counter += 1
    log_iteration(iter_counter)

    current_time = next_beacon_time

    beacons = access_point.send_beacons(current_time)
    next_beacon_time = beacons[0].next_beacon_time
    package_queue.update(beacons)

    while package_queue.length() > 0:
      package = package_queue.get_next()
      current_time = package.time

      package_queue.check_collisions(package)

      package.print()

      for device in device_heap.devices:
        if device == package.sender:
          continue

        new_packages = device.consume(package)
        if new_packages is not None:
          package_queue.update(new_packages)

  average_connect_time = calc_average_connection_time(access_point.connections)

  return iter_counter, average_connect_time


sum_intervals_count = 0
sum_average_connect_time = 0
for i in range(ITERATIONS):
  (intervals_count, average_connect_time) = run_simulation(i + 1)
  sum_intervals_count += intervals_count
  sum_average_connect_time += average_connect_time

average_intervals_count = sum_intervals_count / ITERATIONS
average_connect_time = sum_average_connect_time / ITERATIONS

print('\nMobile stations:', len(initial_mobile_coords))
print('Sls slots:', access_point_config.sls_slots)
print('Max intervals taken to connect all mobile stations:', average_intervals_count,
      ', time: ', average_intervals_count * access_point_config.beacon_interval)
print('Average station time taken to connect:', average_connect_time)
