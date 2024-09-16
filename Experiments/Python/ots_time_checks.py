import pathlib
import string
import tracemalloc
from copy import deepcopy
from datetime import datetime
from hashlib import sha256
import random
from time import time
import matplotlib.pyplot as plt

from cbs import CBS
from hash import HashFunction
from lamport import Lamport
from mtbs import MTBS
from mts import MTS
from otbs import OTBS
from ots import OTS
from tbs import TBS

breakpoints = [int(elem) for elem in [1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7]]
# breakpoints = [int(elem) for elem in [1<<0, 1<<1, 1<<2, 1<<3, 1<<4, 1<<5, 1<<6, 1<<7, 1<<8, 1<<9, 1<<10]]


def check_time(ots: OTS, dt, sign_copy_period, vrfy_repeats, breakpoints):
    path = 'data/' + dt.strftime("%y-%m-%d-%H-%M-%S") + '/'
    filename = ots.__class__.__name__ + '.txt'
    pathlib.Path(path).mkdir(parents=True, exist_ok=True)
    file = open(path + filename, 'w')

    tracemalloc.start()

    ots_range = range(1, breakpoints[-1] + 1)
    ots_gen_time = []
    ots_gen_n_copy_time = []
    ots_key_size = []
    ots_sign_time = []
    ots_sign_n_copy_time = [0]
    ots_sign_size = [0]
    ots_vrfy_time = []
    ots_current_memory = []

    for i in ots_range:
        message = ''.join(random.choice(string.ascii_lowercase) for k in range(10))

        start = time()
        ots_sk, ots_pk = ots.gen()
        ots_gen_time.append(time() - start)

        if i % sign_copy_period == 0:
            current_before, _ = tracemalloc.get_traced_memory()
            ots_sk_copy = deepcopy(ots_sk)
            ots_pk_copy = deepcopy(ots_pk)
            current_after, _ = tracemalloc.get_traced_memory()
            ots_gen_n_copy_time.append(time() - start)
            ots_key_size.append((current_after - current_before) / (1 << 20))
            del ots_sk_copy
            del ots_pk_copy

        start = time()
        sign = ots.sign(ots_sk, message)
        ots_sign_time.append(time() - start)

        if i % sign_copy_period == 0:
            current_before, _ = tracemalloc.get_traced_memory()
            sign_copy = deepcopy(sign)
            current_after, _ = tracemalloc.get_traced_memory()
            ots_sign_n_copy_time.append(time() - start)
            ots_sign_size.append((current_after - current_before) / (1 << 20))
            del sign_copy

        start = time()
        for j in range(vrfy_repeats):
            vrfy = ots.vrfy(ots_pk, sign, message)
        end = time()
        ots_vrfy_time.append((end - start) / vrfy_repeats)

        if vrfy is not True:
            print(i, message)
            break

        current, peak = tracemalloc.get_traced_memory()
        current = current / (1 << 20)
        peak = peak / (1 << 20)
        ots_current_memory.append(current)

        print('\r{0: >9}'.format(i), ' : {0: <21}'.format(ots_sign_time[-1]), ' # {0: <21}'.format(ots_vrfy_time[-1]), ' # {0: <21}'.format(ots_sign_size[-1]), ' # {0: <21}'.format(current), ' # {0: <21}'.format(peak), end="                                                       ")

        if i in breakpoints:
            print('\r{0: >9}'.format(i), ' : {0: <21}'.format(ots_sign_time[-1]), ' # {0: <21}'.format(ots_vrfy_time[-1]), ' # {0: <21}'.format(ots_sign_size[-1]), ' # {0: <21}'.format(current), ' # {0: <21}'.format(peak))
            # mts_sign_checks.append(None)
            # mts_vrfy_checks.append(None)

        file_line = " ".join([str(ots_range[i - 1]), str(ots_gen_time[-1]), str(ots_sign_time[-1]), str(ots_vrfy_time[-1]), str(ots_current_memory[-1])])
        if i % sign_copy_period == 0:
            file_line += " " + str(ots_gen_n_copy_time[-1]) + " " + str(ots_key_size[-1]) + " " + str(ots_sign_n_copy_time[-1]) + " " + str(ots_sign_size[-1])
        file.write(file_line + "\n")

    tracemalloc.stop()
    file.close()

    return ots_range, ots_gen_time, ots_gen_n_copy_time, ots_key_size, ots_sign_time, ots_sign_size, ots_vrfy_time, ots_current_memory


hashF = HashFunction(sha256)
lamport = Lamport(hashF, 256, 123)

dt = datetime.now()
ots_r, ots_gt, ots_gnct, ots_ks, ots_sc, ots_ss, ots_vc, ots_cm = check_time(lamport, dt, 1, 5, breakpoints)

# figure, axis = plt.subplots(2, 2)
#
# axis[0, 0].plot(cbs_r, cbs_sc)
# axis[0, 0].plot(tbs_r, tbs_sc)
# axis[0, 0].plot(otbs_r, otbs_sc)
# axis[0, 0].plot(mtbs_r, mtbs_sc)
# axis[0, 0].set_title("mts_sign_checks")
#
# axis[0, 1].plot(cbs_r, cbs_ss)
# axis[0, 1].plot(tbs_r, tbs_ss)
# axis[0, 1].plot(otbs_r, otbs_ss)
# axis[0, 1].plot(mtbs_r, mtbs_ss)
# axis[0, 1].set_title("mts_sign_size")
#
# axis[1, 0].plot(cbs_r, cbs_vc)
# axis[1, 0].plot(tbs_r, tbs_vc)
# axis[1, 0].plot(otbs_r, otbs_vc)
# axis[1, 0].plot(mtbs_r, mtbs_vc)
# axis[1, 0].set_title("mts_vrfy_checks")
#
# axis[1, 1].plot(cbs_r, cbs_cm)
# axis[1, 1].plot(tbs_r, tbs_cm)
# axis[1, 1].plot(otbs_r, otbs_cm)
# axis[1, 1].plot(mtbs_r, mtbs_cm)
# axis[1, 1].set_title("mts_current_memory")
#
# plt.show()
