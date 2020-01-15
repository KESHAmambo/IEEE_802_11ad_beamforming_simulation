from protocol.device_heap import DeviceHeap
from protocol.devices.access_point import AccessPoint
from protocol.devices.access_point_config import AccessPointConfig
from protocol.devices.mobile import Mobile
from protocol.devices.mobile_config import MobileConfig
from protocol.package_queue import PackageQueue
from ratracer.utils import verbose_uncolored


ITERATIONS = 30

access_point_config = AccessPointConfig()
mobile_config = MobileConfig()

initial_access_pint_coords = [0, 0, 0]

# number_of_mobile_stations = 2
# number_of_mobile_stations = 6
# number_of_mobile_stations = 10
# number_of_mobile_stations = 14
# number_of_mobile_stations = 18
# number_of_mobile_stations = 20
number_of_mobile_stations = 22

initial_mobile_coords = [
  # [0, 10, 10],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
  # [0, 10, 30],
]
for i in range(number_of_mobile_stations):
  initial_mobile_coords.append([0, 10, 30])



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
