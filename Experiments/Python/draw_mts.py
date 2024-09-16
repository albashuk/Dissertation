from matplotlib import pyplot as plt


def fix_name(name):
    if name == "OTBS":
        name = "STBS"
    if name == "MTBS":
        name = "WSTBS"
    return name


def fix_data(data, ratio_limit, size = 10):
    for i in range(size, len(data)):
        expected = 4 * sum(data[i - size // 2 : i]) / size - 2 * sum(data[i - size: i - size // 2]) / size
        ratio = data[i] / expected
        if ratio >= ratio_limit or 1 / ratio >= ratio_limit:
            data[i] = expected


def read_file(datafile_name, fix_data_ = False):
    mts_r, mts_st, mts_vt, mts_cm, mts_snct, mts_ss = [], [], [], [], [], []
    with open("data/to_draw/" + datafile_name + ".txt", 'r') as datafile:
        for line in datafile:
            values = line.split()
            mts_r.append(int(values[0]))
            mts_st.append(float(values[1]))
            mts_vt.append(float(values[2]))
            mts_cm.append(float(values[3]))
            if len(values) > 4:
                mts_snct.append(float(values[4]))
                mts_ss.append(float(values[5]))

    if fix_data_:
        fix_data(mts_st, 10)
        fix_data(mts_vt, 100)
        fix_data(mts_cm, 100)
        fix_data(mts_snct, 100)
        fix_data(mts_ss, 100)

    return mts_r, mts_st, mts_vt, mts_cm, mts_snct, mts_ss


def draw_data(axis, mts_r, mts_st, mts_vt, mts_cm, mts_snct, mts_ss, mts_name):
    axis[0, 0].plot(mts_r, mts_st, label=fix_name(mts_name))
    axis[0, 1].plot(mts_r, mts_vt, label=fix_name(mts_name))
    axis[0, 2].plot(mts_r, mts_cm, label=fix_name(mts_name))
    axis[1, 1].plot(range(10, mts_r[-1] + 1, 10), mts_snct, label=fix_name(mts_name))
    axis[1, 2].plot(range(10, mts_r[-1] + 1, 10), mts_ss, label=fix_name(mts_name))


def draw_data_ind(mts, step, ind, mts_name):
    plt.plot(range(step, mts[0][-1] + 1, step), mts[ind], label=fix_name(mts_name))


def read_n_draw(datafile_name, axis):
    mts_r, mts_st, mts_vt, mts_cm, mts_snct, mts_ss = read_file(datafile_name)
    draw_data(axis, mts_r, mts_st, mts_vt, mts_cm, mts_snct, mts_ss, datafile_name)


def read_n_draw_ind(datafile_name, step, ind):
    mts = read_file(datafile_name, True)
    draw_data_ind(mts, step, ind, datafile_name)


read_n_draw_ind("CBS", 1, 1)
read_n_draw_ind("TBS", 1, 1)
read_n_draw_ind("OTBS", 1, 1)
read_n_draw_ind("MTBS", 1, 1)
plt.legend()


# figure, axis = plt.subplots(2, 3)
# axis[0, 0].set_title("mts_sign_time")
# axis[0, 1].set_title("mts_vrfy_time")
# axis[0, 2].set_title("mts_current_memory")
# axis[1, 1].set_title("mts_sign_n_copy_time")
# axis[1, 2].set_title("mts_sign_size")
# axis[0, 0].legend()
# axis[0, 1].legend()
# axis[0, 2].legend()
# axis[1, 1].legend()
# axis[1, 1].legend()

# read_n_draw("CBS", axis)
# read_n_draw("TBS", axis)
# read_n_draw("OTBS", axis)
# read_n_draw("MTBS", axis)

# cbs_r, cbs_st, cbs_vt, cbs_cm, cbs_snct, cbs_ss = read_file("CBS")
# tbs_r, tbs_st, tbs_vt, tbs_cm, tbs_snct, tbs_ss = read_file("TBS")
# otbs_r, otbs_st, otbs_vt, otbs_cm, otbs_snct, otbs_ss = read_file("OTBS")
# mtbs_r, mtbs_st, mtbs_vt, mtbs_cm, mtbs_snct, mtbs_ss = read_file("MTBS")

# axis[0, 0].plot(cbs_r, cbs_st, label="cbs")
# axis[0, 0].plot(tbs_r, tbs_st, label="tbs")
# axis[0, 0].plot(otbs_r, otbs_st, label="otbs")
# axis[0, 0].plot(mtbs_r, mtbs_st, label="mtbs")
# axis[0, 0].set_title("mts_sign_time")
#
# axis[0, 1].plot(cbs_r, cbs_vt, label="cbs")
# axis[0, 1].plot(tbs_r, tbs_vt, label="tbs")
# axis[0, 1].plot(otbs_r, otbs_vt, label="otbs")
# axis[0, 1].plot(mtbs_r, mtbs_vt, label="mtbs")
# axis[0, 1].set_title("mts_vrfy_time")
#
# axis[0, 2].plot(cbs_r, cbs_cm, label="cbs")
# axis[0, 2].plot(tbs_r, tbs_cm, label="tbs")
# axis[0, 2].plot(otbs_r, otbs_cm, label="otbs")
# axis[0, 2].plot(mtbs_r, mtbs_cm, label="mtbs")
# axis[0, 2].set_title("mts_current_memory")
#
# axis[1, 1].plot(range(10, cbs_r[-1] + 1, 10), cbs_snct, label="cbs")
# axis[1, 1].plot(range(10, tbs_r[-1] + 1, 10), tbs_snct, label="tbs")
# axis[1, 1].plot(range(10, otbs_r[-1] + 1, 10), otbs_snct, label="otbs")
# axis[1, 1].plot(range(10, mtbs_r[-1] + 1, 10), mtbs_snct, label="mtbs")
# axis[1, 1].set_title("mts_sign_n_copy_time")
#
# axis[1, 2].plot(range(10, cbs_r[-1] + 1, 10), cbs_ss, label="cbs")
# axis[1, 2].plot(range(10, tbs_r[-1] + 1, 10), tbs_ss, label="tbs")
# axis[1, 2].plot(range(10, otbs_r[-1] + 1, 10), otbs_ss, label="otbs")
# axis[1, 2].plot(range(10, mtbs_r[-1] + 1, 10), mtbs_ss, label="mtbs")
# axis[1, 2].set_title("mts_sign_size")

plt.show()
