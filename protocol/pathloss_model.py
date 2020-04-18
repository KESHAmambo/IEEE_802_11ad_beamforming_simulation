from protocol.protocol_params import CONTROL_PHY_FREQ
from ratracer.radio import KRayPathloss
from ratracer.radio import Tracer, build

SCENE = {
  (10, 0, 0): (-1, 0, 0),
  (-10, 0, 0): (1, 0, 0),
  (0, 2, 0): (0, -1, 0),
  (0, -2, 0): (0, 1, 0),
}

tracer = Tracer(build(SCENE, CONTROL_PHY_FREQ))

PATHLOSS_MODEL = KRayPathloss(tracer, CONTROL_PHY_FREQ)
