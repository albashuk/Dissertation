breakpoints = [int(elem) for elem in [1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7]]

def read_file(datafile_name):
    hash_r, hash_t, hash_m = [], [], []
    with open("data/to_draw/" + datafile_name + ".txt", 'r') as datafile:
        for line in datafile:
            values = line.split()
            hash_r.append(int(values[0]))
            hash_t.append(float(values[1]))
            hash_m.append(float(values[2]))

    return hash_r, hash_t, hash_m

hash_r, hash_t, hash_m = read_file("builtin_function_or_method")
for i in breakpoints:
    print('{0: >9}'.format(hash_r[i - 1]), ' : {0: <.11f}'.format(hash_t[i - 1]), ' # {0: <.3f}'.format(hash_m[i - 1]))
