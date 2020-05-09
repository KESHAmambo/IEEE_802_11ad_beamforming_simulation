# all transmission speeds are in bytes/seconds
from protocol.packages.responder_sector_sweep import SIZE as RESPONDER_SSW_SIZE
from protocol.packages.ssw_ack import SIZE as SSW_ACK_SIZE
from protocol.packages.ssw_feedback import SIZE as SSW_FEEDBACK_SIZE

CONTROL_PHY_SPEED = 27500000
CONTROL_PHY_FREQ = 2160e6
MIN_SNR = 5.01189
NOISE_POWER = 7.9e-9
REFLECTIONS = 1

MAX_SECTORS_FOR_MOBILE = 64
SLS_SLOT_DURATION = MAX_SECTORS_FOR_MOBILE * (RESPONDER_SSW_SIZE \
                         + SSW_FEEDBACK_SIZE \
                         + SSW_ACK_SIZE) / CONTROL_PHY_SPEED