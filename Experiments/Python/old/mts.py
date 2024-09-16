from ots import OTS
from hashlib import sha256
from time import time

class MTS:
    def __init__(self, ots):
        self.ots = ots

    def gen(self, key):
        return self.ots.gen(key)

    def sign(self, sk, message, key):
        sk_new, pk_new = self.gen(key)
        sign = self.ots.sign(sk, str(message) + " " + str(pk_new))
        return sign, sk_new, pk_new

    def vrfy(self, pk0, signs):
        pk = pk0
        for i in range(len(signs)):
            message, pk_new, sign = signs[i]
            if not self.ots.vrfy(pk, sign, str(message) + " " + str(pk_new)):
                return False
            pk = pk_new
        return True


seed_key = 123
def seed():
    return int(time() * 1000) + seed_key

users = 32
messages = ["Hello", "foo"]
ots = OTS(sha256, 256, users)
mts = MTS(ots)
keys = []
cur_keys = []
signs = []

for i in range(users):
    keys.append(mts.gen((seed(), i)))
    cur_keys.append(keys[-1])
    signs.append([])

for message in messages:
    for i in range(users):
        sign, sk, pk = mts.sign(cur_keys[i][0], message, (seed(), i))
        signs[i].append((message, pk, sign))
        cur_keys[i] = (sk, pk)

    for i in range(users):
        print("Vrfy", message, i, mts.vrfy(keys[i][1], signs[i]))
