from hashlib import sha256
import random

# input_ = "foo" # input('Enter something: ')
# print(input_.encode('utf-8'))
# print(type(input_.encode('utf-8')))
# print(sha256(input_.encode('utf-8')))
# print(int(sha256(input_.encode('utf-8')).hexdigest(), 16) & 0b10)

class OTS:
    class Key:
        def __init__(self, key, iter):
            self.key = key
            self.iter = iter

        def __str__(self):
            return self.key.hexdigest() + " " + str(self.iter)

    def __init__(self, hash, hash_size, m):
        self.hash = hash
        self.hash_size = hash_size
        self.m = m
        self.w = (hash_size - 1) // self.m + 1
        self.w_pow = 2 ** self.w
        self.mask = self.w_pow - 1

    def gen(self, seed): # (sk, iter), (pk, iter)
        seed, iter = seed
        random.seed(seed)
        sk = self.hash(repr(random.randint(0, 1e9)).encode())
        pk = sk
        for i in range(self.w_pow):
            pk = self.hash(pk.digest())
        return self.Key(sk, iter), self.Key(pk, iter)

    def sign(self, sk: Key, message):
        sk, iter = sk.key, sk.iter
        N_i = self.__get_N(message, iter)
        sign = sk
        for i in range(self.w_pow - N_i):
            sign = self.hash(sign.digest())
        return sign

    def vrfy(self, pk: Key, sign, message):
        pk, iter = pk.key, pk.iter
        N_i = self.__get_N(message, iter)
        for i in range(N_i):
            sign = self.hash(sign.digest())
        return pk.digest() == sign.digest()

    def __get_N(self, message, iter):
        hash = int(self.hash(message.encode('utf-8')).hexdigest(), 16)
        return (hash >> (iter * self.w)) & self.mask

