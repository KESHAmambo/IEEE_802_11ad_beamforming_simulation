"""
Distribution plot options
=========================

"""
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt


avgArr6 = [102.40036130909093, 102.40036130909093, 102.40036130909093, 102.40036130909093, 102.40036130909093, 102.40036130909093, 102.40036130909093, 102.40036130909093, 102.40036130909093, 204.80023563636368, 204.80023563636368, 204.80023563636368, 204.80023563636368, 204.80023563636368, 204.80023563636368, 204.80023563636368, 204.80023563636368, 204.80023563636368, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80029847272732, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 204.80036130909096, 307.2002356363637, 307.2002356363637, 307.2002356363637, 307.2002356363637, 307.2002356363637, 307.2002356363637, 307.2002356363637, 307.2002356363637, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.2002984727273, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 307.20036130909097, 409.6002984727273, 409.60036130909094, 409.60036130909094, 409.60036130909094, 409.60036130909094, 512.0002984727272, 614.4002984727272, 614.4003613090907, 614.4003613090907, 716.8003613090907]
avgArr14 = [512.0002984727272, 512.0002984727272, 512.0003613090907, 614.4002356363635, 614.4002984727272, 614.4002984727272, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 614.4003613090907, 716.8002356363635, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8002984727271, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 716.8003613090907, 819.2002356363635, 819.2002356363635, 819.2002356363635, 819.2002356363635, 819.2002984727271, 819.2002984727271, 819.2002984727271, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 819.2003613090907, 921.6002356363634, 921.6002984727271, 921.6002984727271, 921.6002984727271, 921.6002984727271, 921.6002984727271, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 921.6003613090907, 1024.0002356363636, 1024.0002984727273, 1024.000361309091, 1024.000361309091, 1024.000361309091, 1024.000361309091, 1024.000361309091, 1024.000361309091, 1024.000361309091, 1126.4002356363635, 1126.4002984727272, 1126.4003613090908, 1228.8002984727273, 1228.800361309091, 1331.2002984727274, 1331.2002984727274, 1331.200361309091, 1536.0003613090912, 1638.400235636364]
avgArr25 = [1331.2002984727274, 1638.4003613090913, 1740.800235636364, 1740.8002984727277, 1740.8003613090914, 1740.8003613090914, 1843.2002356363641, 1843.2002984727278, 1843.2003613090915, 1843.2003613090915, 1945.600298472728, 1945.600298472728, 1945.600298472728, 1945.6003613090916, 2048.0002984727284, 2048.0002984727284, 2048.0002984727284, 2048.000361309092, 2048.000361309092, 2048.000361309092, 2150.400235636365, 2150.400235636365, 2150.4002984727285, 2150.400361309092, 2150.400361309092, 2150.400361309092, 2150.400361309092, 2252.8003613090923, 2252.8003613090923, 2355.2003613090924, 2355.2003613090924, 2355.2003613090924, 2355.2003613090924, 2355.2003613090924, 2457.600298472729, 2457.6003613090925, 2457.6003613090925, 2457.6003613090925, 2457.6003613090925, 2457.6003613090925, 2560.000298472729, 2560.0003613090926, 2560.0003613090926, 2560.0003613090926, 2560.0003613090926, 2560.0003613090926, 2560.0003613090926, 2662.4002356363653, 2662.400298472729, 2662.400298472729, 2662.4003613090927, 2662.4003613090927, 2764.800298472729, 2764.800298472729, 2764.8003613090928, 2764.8003613090928, 2867.200298472729, 2867.200361309093, 2867.200361309093, 2867.200361309093, 2969.600361309093, 3072.0002984727294, 3072.0002984727294, 3072.0002984727294, 3072.0002984727294, 3072.0002984727294, 3174.4002984727294, 3174.4002984727294, 3174.400361309093, 3174.400361309093, 3174.400361309093, 3276.800361309093, 3276.800361309093, 3379.200235636366, 3379.2003613090933, 3481.6002984727297, 3481.6002984727297, 3481.6003613090934, 3481.6003613090934, 3686.4002356363662, 3686.4003613090936, 3686.4003613090936, 3788.80029847273, 3788.8003613090937, 3993.60029847273, 3993.60029847273, 3993.600361309094, 3993.600361309094, 4096.000361309092, 4096.000361309092, 4198.400298472728, 4198.400361309092, 4198.400361309092, 4300.800361309091, 4608.000235636363, 4608.00036130909, 5017.6002356363615, 5017.600361309089, 5120.000235636361, 7168.000361309081]

a6 = np.array(avgArr6)
a14 = np.array(avgArr14)
a25 = np.array(avgArr25)

sns.set(style="white", palette="muted", color_codes=True)

f, axes = plt.subplots(figsize=(7, 7))

axlabel = 'Максимальное время, мс'

sns.distplot(a6, color="#4285f4", ax=axes, axlabel=axlabel, label='6 станций')
sns.distplot(a14, color="#ea4335", ax=axes, axlabel=axlabel, label='14 станций')
sns.distplot(a25, color="#fbbc04", ax=axes, axlabel=axlabel, label='25 станций')

axes.legend()

plt.setp(axes)
plt.tight_layout()
plt.show()