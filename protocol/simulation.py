from random import randrange

from protocol.device_heap import DeviceHeap
from protocol.devices.access_point import AccessPoint
from protocol.devices.access_point_config import AccessPointConfig
from protocol.devices.mobile import Mobile
from protocol.devices.mobile_config import MobileConfig
from protocol.package_queue import PackageQueue
from protocol.pathloss_model import CARRIAGE_HALF_WIDTH
from ratracer.utils import verbose_uncolored
from protocol.protocol_params import SLS_SLOT_DURATION

ZONE_LENGTH = 4.56

DEVICE_MARGIN = 0.05
PADDING = 0.05
MIN_X_COORD = -ZONE_LENGTH
MAX_X_COORD = 0
MIN_Y_COORD = -CARRIAGE_HALF_WIDTH + PADDING
MAX_Y_COORD = CARRIAGE_HALF_WIDTH - PADDING


ITERATIONS = 100
SLS_SLOTS = 3
SLS_DYNAMIC = True
SLS_DEGREE = 0.3
initial_access_pint_coords = [
  0,
  0,
  0.
]

access_point_config = AccessPointConfig(SLS_SLOTS, SLS_DYNAMIC, SLS_DEGREE)
mobile_config = MobileConfig()


LENGTH = -MIN_X_COORD + MAX_X_COORD
WIDTH = -MIN_Y_COORD + MAX_Y_COORD
MARGINS_IN_LENGTH = LENGTH // DEVICE_MARGIN
MARGINS_IN_WIDTH = WIDTH // DEVICE_MARGIN


def calc_average_connection_time(connections):
  sum_time = 0
  for device_id, connection in connections.items():
    sum_time += connection.time

  return sum_time / len(connections)


def calc_max_connection_time(connections):
  max_time = 0
  for device_id, connection in connections.items():
    if max_time < connection.time:
      max_time = connection.time

  return max_time


def calc_sls_slots_sum(sls_slots_history):
  sls_slots_sum = 0
  for slots in sls_slots_history:
    sls_slots_sum += slots

  return sls_slots_sum


def calc_avg_history(sls_slots_histories):
  max_len = 0
  for history in sls_slots_histories:
    if len(history) > max_len:
      max_len = len(history)

  for i in range(max_len):
    for history in sls_slots_histories:
      if len(history) <= i:
        history.append(SLS_SLOTS)

  avg_history = []

  for i in range(max_len):
    sum_slots = 0
    for history in sls_slots_histories:
      sum_slots += history[i]

    avg_slots = sum_slots / len(sls_slots_histories)
    avg_history.append(avg_slots)

  return avg_history


@verbose_uncolored
def log_iteration(iteration):
  print("\nIteration", iteration)


def run_simulation(simulation_count, number_of_mobile_stations):
  if simulation_count % (ITERATIONS / 10) == 0:
    print('--- Simulation run --- (', simulation_count, '/', ITERATIONS, ')')

  initial_mobile_coords = []
  for i in range(number_of_mobile_stations):
    x = DEVICE_MARGIN * randrange(0, MARGINS_IN_LENGTH) + MIN_X_COORD
    y = DEVICE_MARGIN * randrange(0, MARGINS_IN_WIDTH) + MIN_Y_COORD
    initial_mobile_coords.append([x, y, 0])

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
  max_time = calc_max_connection_time(access_point.connections)
  sls_slots_sum = calc_sls_slots_sum(access_point.sls_slots_history)

  return iter_counter, average_connect_time, max_time, sls_slots_sum, access_point.sls_slots_history


def run_simulation_series(number_of_mobile_stations):
  sum_intervals_count = 0
  sum_average_connect_time = 0
  sls_slots_sum = 0
  avg_connect_times = []
  max_connect_times = []
  sls_slots_histories = []
  for i in range(ITERATIONS):
    (intervals_count, average_connect_time, max_time, sls_slots, sls_slots_history) = run_simulation(i + 1, number_of_mobile_stations)
    sum_intervals_count += intervals_count
    sum_average_connect_time += average_connect_time
    sls_slots_sum += sls_slots
    avg_connect_times.append(average_connect_time)
    max_connect_times.append(max_time)
    sls_slots_histories.append(sls_slots_history)

  average_intervals_count = sum_intervals_count / ITERATIONS
  res_average_connect_time = sum_average_connect_time / ITERATIONS
  avg_sls_slots_sum = sls_slots_sum / ITERATIONS

  avg_history = calc_avg_history(sls_slots_histories)
  avg_connect_times.sort()
  max_connect_times.sort()

  print('\nMobile stations:', number_of_mobile_stations)
  print('Avg max intervals taken to connect all mobile stations:', average_intervals_count)
  print('Avg max time before connection: ', (average_intervals_count - 1) * access_point_config.beacon_interval)
  print('Average station time taken to connect:', res_average_connect_time)
  print('Avg connection times:', avg_connect_times)
  print('Max connection times:', max_connect_times)
  print('SLS slots avg sum:', avg_sls_slots_sum)
  print('SLS slots avg sum time:', avg_sls_slots_sum * SLS_SLOT_DURATION)
  print('SLS slots avg history:', avg_history)
  print('\n')


if __name__ == '__main__':
  print('SLS dynamic', SLS_DYNAMIC)
  print('SLS degree', SLS_DEGREE)
  print('AP position', initial_access_pint_coords)
  print('AP sectors', access_point_config.sectors)
  print('Mob sectors:', mobile_config.sectors)
  print('Sls slots:', access_point_config.sls_slots)
  print('\n')

  # run_simulation_series(1)
  # run_simulation_series(2)
  # run_simulation_series(4)
  # run_simulation_series(6)
  # run_simulation_series(10)
  # run_simulation_series(14)
  # run_simulation_series(18)
  # run_simulation_series(22)
  # run_simulation_series(25)

  run_simulation_series(35)

  # run_simulation_series(50)

