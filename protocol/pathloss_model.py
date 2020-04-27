from protocol.protocol_params import CONTROL_PHY_FREQ
from ratracer.radio import KRayPathloss
from ratracer.radio import Tracer, build

CARRIAGE_HALF_LENGTH = 19.14 / 2
CARRIAGE_HALF_WIDTH = 2.68 / 2

SCENE = {
  (CARRIAGE_HALF_LENGTH, 0, 0): (-1, 0, 0),
  (-CARRIAGE_HALF_LENGTH, 0, 0): (1, 0, 0),
  (0, CARRIAGE_HALF_WIDTH, 0): (0, -1, 0),
  (0, -CARRIAGE_HALF_WIDTH, 0): (0, 1, 0),
}

tracer = Tracer(build(SCENE, CONTROL_PHY_FREQ))

PATHLOSS_MODEL = KRayPathloss(tracer, CONTROL_PHY_FREQ)
