breakpoints = [int(elem) for elem in [1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7]]

def read_file(datafile_name):
    ots_r, ots_gt, ots_st, ots_vt, ots_cm, ots_gnct, ots_ks, ots_snct, ots_ss = [], [], [], [], [], [], [], [], []
    with open("data/to_draw/" + datafile_name + ".txt", 'r') as datafile:
        for line in datafile:
            values = line.split()
            ots_r.append(int(values[0]))
            ots_gt.append(float(values[1]))
            ots_st.append(float(values[2]))
            ots_vt.append(float(values[3]))
            ots_cm.append(float(values[4]))
            if len(values) > 5:
                ots_gnct.append(float(values[5]))
                ots_ks.append(float(values[6]))
                ots_snct.append(float(values[7]))
                ots_ss.append(float(values[8]))

    return ots_r, ots_gt, ots_st, ots_vt, ots_cm, ots_gnct, ots_ks, ots_snct, ots_ss

ots_r, ots_gt, ots_st, ots_vt, ots_cm, ots_gnct, ots_ks, ots_snct, ots_ss = read_file("Lamport")
for i in ots_r:
    if i < len(ots_r):
        ots_gt[i] += ots_gt[i - 1]
        ots_st[i] += ots_st[i - 1]
        ots_vt[i] += ots_vt[i - 1]
        ots_cm[i] += ots_cm[i - 1]
        ots_gnct[i] += ots_gnct[i - 1]
        ots_ks[i] += ots_ks[i - 1]
        ots_snct[i] += ots_snct[i - 1]
        ots_ss[i] += ots_ss[i - 1]

    ots_gt[i - 1] /= i
    ots_st[i - 1] /= i
    ots_vt[i - 1] /= i
    ots_cm[i - 1] /= i
    ots_gnct[i - 1] /= i
    ots_ks[i - 1] /= i
    ots_snct[i - 1] /= i
    ots_ss[i - 1] /= i

for i in breakpoints[:6]:
    print('{0: >9}'.format(ots_r[i - 1]), ' : {0: <.6f}'.format(ots_gt[i - 1]), ' # {0: <.6f}'.format(ots_st[i - 1]),
          ' # {0: <.6f}'.format(ots_vt[i - 1]), ' # {0: <.4f}'.format(ots_cm[i - 1]), ' # {0: <.6f}'.format(ots_gnct[i - 1]),
          ' # {0: <.4f}'.format(ots_ks[i - 1]), ' # {0: <.6f}'.format(ots_snct[i - 1]), ' # {0: <.4f}'.format(ots_ss[i - 1]))
i = len(ots_r)
print('{0: >9}'.format(ots_r[i - 1]), ' : {0: <.6f}'.format(ots_gt[i - 1]), ' # {0: <.6f}'.format(ots_st[i - 1]),
      ' # {0: <.6f}'.format(ots_vt[i - 1]), ' # {0: <.4f}'.format(ots_cm[i - 1]), ' # {0: <.6f}'.format(ots_gnct[i - 1]),
      ' # {0: <.4f}'.format(ots_ks[i - 1]), ' # {0: <.6f}'.format(ots_snct[i - 1]), ' # {0: <.4f}'.format(ots_ss[i - 1]))