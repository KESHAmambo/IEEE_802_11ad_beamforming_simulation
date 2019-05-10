from protocol import settings

class PackageQueue:
  def __init__(self):
    self.queue = set()

  def update(self, packages):
    self.queue.update(packages)

  def add(self, package):
    self.queue.add(package)

  def length(self):
    return len(self.queue)

  def print(self):
    print(self.queue)

  def remove(self, package):
    self.queue.remove(package)

  def get_next(self):
    next_package = None

    for package in self.queue:
      if next_package is None or package.end_time < next_package.end_time:
        next_package = package

    self.queue.remove(next_package)

    return next_package

  def check_collisions(self, checked_package):
    for package in self.queue:
      check_if_collide(checked_package, package)


def check_if_collide(main_package, other_package):
  main_package_end = main_package.time + main_package.size / main_package.send_params.speed
  other_package_end = other_package.time + other_package.size / other_package.send_params.speed

  if main_package_end - other_package.time > settings['time_precision'] \
      and other_package_end - main_package.time > settings['time_precision']:
    other_package.collisions.add(main_package)
    main_package.collisions.add(other_package)
