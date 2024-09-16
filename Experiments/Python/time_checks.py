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
from tbs import TBS

breakpoints = [int(elem) for elem in [1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7]]
# breakpoints = [int(elem) for elem in [1<<0, 1<<1, 1<<2, 1<<3, 1<<4, 1<<5, 1<<6, 1<<7, 1<<8, 1<<9, 1<<10]]


def check_time(mts: MTS, dt, sign_copy_period, vrfy_repeats, breakpoints):
    path = 'data/' + dt.strftime("%y-%m-%d-%H-%M-%S") + '/'
    filename = mts.__class__.__name__ + '.txt'
    pathlib.Path(path).mkdir(parents=True, exist_ok=True)
    file = open(path + filename, 'w')

    tracemalloc.start()

    mts_sk, mts_pk = mts.gen()

    mts_range = range(1, breakpoints[-1] + 1)
    mts_sign_time = []
    mts_sign_n_copy_time = [0]
    mts_sign_size = [0]
    mts_vrfy_time = []
    mts_current_memory = []

    for i in mts_range:
        message = ''.join(random.choice(string.ascii_lowercase) for k in range(10))

        start = time()
        sign = mts.sign(mts_sk, message)
        mts_sign_time.append(time() - start)

        if i % sign_copy_period == 0:
            current_before, _ = tracemalloc.get_traced_memory()
            sign_copy = deepcopy(sign)
            current_after, _ = tracemalloc.get_traced_memory()
            mts_sign_n_copy_time.append(time() - start)
            mts_sign_size.append((current_after - current_before) / (1 << 20))
            del sign_copy

        start = time()
        for j in range(vrfy_repeats):
            vrfy = mts.vrfy(mts_pk, sign, message)
        end = time()
        mts_vrfy_time.append((end - start) / vrfy_repeats)

        if vrfy is not True:
            print(i, message)
            break

        current, peak = tracemalloc.get_traced_memory()
        current = current / (1 << 20)
        peak = peak / (1 << 20)
        mts_current_memory.append(current)

        print('\r{0: >9}'.format(i), ' : {0: <21}'.format(mts_sign_time[-1]), ' # {0: <21}'.format(mts_vrfy_time[-1]), ' # {0: <21}'.format(mts_sign_size[-1]), ' # {0: <21}'.format(current), ' # {0: <21}'.format(peak), end="                                                       ")

        if i in breakpoints:
            print('\r{0: >9}'.format(i), ' : {0: <21}'.format(mts_sign_time[-1]), ' # {0: <21}'.format(mts_vrfy_time[-1]), ' # {0: <21}'.format(mts_sign_size[-1]), ' # {0: <21}'.format(current), ' # {0: <21}'.format(peak))
            # mts_sign_checks.append(None)
            # mts_vrfy_checks.append(None)

        file_line = " ".join([str(mts_range[i - 1]), str(mts_sign_time[-1]), str(mts_vrfy_time[-1]), str(mts_current_memory[-1])])
        if i % sign_copy_period == 0:
            file_line += " " + str(mts_sign_n_copy_time[-1]) + " " + str(mts_sign_size[-1])
        file.write(file_line + "\n")

    tracemalloc.stop()
    file.close()

    return mts_range, mts_sign_time, mts_sign_size, mts_vrfy_time, mts_current_memory


hashF = HashFunction(sha256)
lamport = Lamport(hashF, 256, 123)
cbs = CBS(hashF, 256, lamport)
tbs = TBS(hashF, 256, lamport)
otbs = OTBS(hashF, 256, lamport)
mtbs = MTBS(hashF, 256, lamport)

dt = datetime.now()
# cbs_r, cbs_sc, cbs_ss, cbs_vc, cbs_cm = check_time(cbs, dt, 10, 1, breakpoints[0:4])
# otbs_r, otbs_sc, otbs_ss, otbs_vc, otbs_cm = check_time(otbs, dt, 10, 5, breakpoints[0:6])
# mtbs_r, mtbs_sc, mtbs_ss, mtbs_vc, mtbs_cm = check_time(mtbs, dt, 10, 5, breakpoints[0:6])
tbs_r, tbs_sc, tbs_ss, tbs_vc, tbs_cm = check_time(tbs, dt, 10, 1, breakpoints[0:6])

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
