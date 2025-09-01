from matplotlib import pyplot as plt
import ast
import pandas as pd
import plotly.io as pio
import plotly.graph_objects as go
import numpy as np

def preprocess_data(data):
    for i in range(len(data[3])):
        data[3][i] /= 1024 * 1024 # Bs to MBs

def read_file(path):
    with open(path, 'r') as datafile:
        data = [ast.literal_eval(line) for line in datafile]
        preprocess_data(data)
        data_time_range = list(range(len(data[0])))
        data_size_range = list(range(9, len(data[0]), 10))
        df_time = pd.DataFrame({'x': data_time_range, 'st': data[0], 'sct': data[1], 'vt': data[2]})
        df_size = pd.DataFrame({'x': data_size_range, 'ss': data[3]})
        return df_time, df_size

def load_nary_tbs_data(configs):
    d = []
    for config in configs:
        path = 'data/to_draw/NaryTBS/' + config['name'] + '.txt'
        d.append(list(read_file(path)))
    return d


if __name__ == "__main__":
    # data
    configs = [{'name': '2_0_0', 'color': 'red'}, {'name': '4_0_0', 'color': 'blue'}]
    d = load_nary_tbs_data(configs)

    # settings
    pio.renderers.default = 'browser'
    # st, sct, vt, ss
    mn = 'ss'
    title = ('<b>'
             + 'STBS та WSTBS'
             + '</b>')
    bb = 0
    lb = 0
    tb = 1000
    rb = 100000
    clean_data = 0
    mean_window = 10
    line_mode = 'lines'
    line_width = 3
    show = [0, 1]

    bi = 1 if mn in ['ss'] else 0
    for d_ in d:
        d_[bi] = d_[bi][d_[bi]['x'] <= rb]

        if clean_data != 0:
            mean = d_[bi][mn].mean()
            std = d_[bi][mn].std()
            d_[bi] = d_[bi][np.abs(d_[bi][mn] - mean) < 3 * std]
            d_[bi][mn] = d_[bi][mn].rolling(window=mean_window).mean()
            mean = d_[bi][mn].mean()
            std = d_[bi][mn].std()
            d_[bi] = d_[bi][np.abs(d_[bi][mn] - mean) < 10 * std]

    # setting boarders
    abb = bb
    alb = lb
    atb = min(tb, max([max(d[i][bi][mn]) for i in show]))
    arb = min(rb, max([max(d[i][0]['x']) for i in show]))
    delta_y = min([min(d[i][bi][mn]) for i in show])

    padding_x = (arb - alb) * 0.05
    padding_y = (atb - abb) * 0.05

    abb -= padding_y
    alb -= padding_x
    atb += padding_y + delta_y
    arb += padding_x

    fig = go.Figure()

    # adding lines
    for ind in show:
        fig.add_trace(go.Scatter(x=d[ind][bi]['x'], y=d[ind][bi][mn],
                                 mode=line_mode,
                                 name=configs[ind]['name'],
                                 line=dict(color=configs[ind]['color'], width=line_width)))

    text_color = 'rgb(75, 75, 75)'
    fig.update_layout(
        xaxis=dict(
            range=[alb, arb],
            title=dict(
                text=title,
                font=dict(size=50, color=text_color, family="Arial"),
            ),
            tickfont=dict(size=35, color=text_color, family="Arial"),
            showgrid=True,
        ),
        yaxis=dict(
            range=[abb, atb],
            tickfont=dict(size=35, color=text_color, family="Arial"),
            showgrid=True,
        ),
        legend=dict(
            x=0.999,
            y=0.999,
            xanchor='right',
            yanchor='top',
            bgcolor='rgba(255,255,255,1)',  # Optional: semi-transparent background
            bordercolor='Black',
            borderwidth=1,
            font=dict(size=30, color=text_color, family="Arial"),
        )
    )
    fig.show()