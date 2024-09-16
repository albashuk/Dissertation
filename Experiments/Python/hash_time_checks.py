import pathlib
import string
import tracemalloc
from datetime import datetime
from hashlib import sha256
import random
from time import time

from hash import HashFunction

breakpoints = [int(elem) for elem in [1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7]]


def check_time(hashF: HashFunction, dt, breakpoints):
    path = 'data/' + dt.strftime("%y-%m-%d-%H-%M-%S") + '/'
    filename = hashF.hashF.__class__.__name__ + '.txt'
    pathlib.Path(path).mkdir(parents=True, exist_ok=True)
    file = open(path + filename, 'w')

    hash_range = range(1, breakpoints[-1] + 1)
    foo = hashF(''.join(random.choice(string.ascii_lowercase) for k in range(10)).encode())

    time_checks = []
    start = time()
    for i in hash_range:
        if i % 100 == 0:
            print("\r", i, end="                                                       ")
        tmp = hashF(repr(random.randint(0, 1e9)).encode())
        time_checks.append(time() - start)

    mem_checks = [0]
    hash_list = []

    tracemalloc.start()
    start, _ = tracemalloc.get_traced_memory()

    for i in hash_range:
        if i % 100 == 0:
            print("\r", i, end="                                                       ")
        current_before, _ = tracemalloc.get_traced_memory()
        hash_list.append(hashF(repr(random.randint(0, 1e9)).encode()))
        current_after, _ = tracemalloc.get_traced_memory()
        mem_checks.append(mem_checks[-1] + current_after - current_before)

    end, _ = tracemalloc.get_traced_memory()
    tracemalloc.stop()

    mem_checks = mem_checks[1:]

    for i in hash_range:
        file_line = " ".join([str(hash_range[i - 1]), str(time_checks[i - 1] / i), str(mem_checks[i - 1] / i)])
        file.write(file_line + "\n")

        print('\r{0: >9}'.format(i), ' : {0: <21}'.format(time_checks[i - 1] / i), ' # {0: <21}'.format(mem_checks[i - 1] / i), end="                                                       ")
        if i in breakpoints:
            print('\r{0: >9}'.format(i), ' : {0: <21}'.format(time_checks[i - 1] / i), ' # {0: <21}'.format(mem_checks[i - 1] / i))

    print('\r{0: >9}'.format(i), ' : {0: <21}'.format(time_checks[i - 1] / i),' # {0: <21}'.format(mem_checks[i - 1] / i), end / i)

    file.close()


dt = datetime.now()
hashF = HashFunction(sha256)
check_time(hashF, dt, breakpoints[:7])
# print(len(hashF(("foo").encode()).hexdigest()))
# print(hashF(("foo").encode()).hexdigest())
