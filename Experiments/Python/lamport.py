import random
from copy import deepcopy
from typing import List

from hash import Hash, HashFunction
from ots import OTS


class Lamport(OTS):

    class SecKey(OTS.SecKey):
        def __init__(self, VALUES: List[List[Hash]]):
            self.VALUES = VALUES

    class PubKey(OTS.PubKey):
        def __init__(self, VALUES: List[List[Hash]], HASH: Hash):
            self.VALUES = VALUES
            self.HASH = HASH

        def __eq__(self, other) -> bool:
            return self.VALUES == other.VALUES and self.HASH == other.HASH

        def __ne__(self, other) -> bool:
            return not self.__eq__(other)

        def hash(self) -> Hash:
            return self.HASH

    class Sign(OTS.Sign):
        def __init__(self, VALUES: List[Hash]):
            self.VALUES = VALUES

    def __init__(self, hashF: HashFunction, hash_size: int, seed_key: int):
        if not issubclass(self.__class__, OTS):
            raise NotImplementedError
        super().__init__(hashF, hash_size, seed_key)

    def gen(self) -> {SecKey, PubKey}:
        random.seed(self._seed())
        sk = self.SecKey([[self.hashF(repr(random.randint(0, 1e9)).encode()) for j in range(self.hash_size)] for i in range(2)])
        pk = [[self.hashF(sk.VALUES[i][j].digest()) for j in range(self.hash_size)] for i in range(2)]
        pk = self.PubKey(pk, self.__mtx_of_hashes_to_hash(pk))
        return sk, pk

    def sign(self, sk: SecKey, message: str) -> Sign:
        message = self.__msg_to_hash_int(message)
        return self.Sign([sk.VALUES[((message >> j) & 0b1)][j] for j in range(self.hash_size)])

    def vrfy(self, pk: PubKey, sign: Sign, message: str) -> bool:
        message = self.__msg_to_hash_int(message)
        for j in range(self.hash_size):
            if self.hashF(sign.VALUES[j].digest()).digest() != pk.VALUES[((message >> j) & 0b1)][j].digest():
                return False
        return True

    def __msg_to_hash_int(self, message: str):
        return int(self.hashF(message.encode()).hexdigest(), 16)

    def __mtx_of_hashes_to_hash(self, mtx: List[List[any]]):
        hash = self.hashF()
        for list in mtx:
            for element in list:
                hash.update(element.digest())
        return hash


if __name__ == "__main__":
    from hashlib import sha256

    hashF = HashFunction(sha256)
    lamport = Lamport(hashF, 256, 123)
    sk, pk = lamport.gen()
    # print([sk.values[0][j].hexdigest() for j in range(256)])
    # print([sk.values[1][j].hexdigest() for j in range(256)])
    # print([pk.values[0][j].hexdigest() for j in range(256)])
    # print([pk.values[1][j].hexdigest() for j in range(256)])
    message1 = "foo1"
    sign1 = lamport.sign(sk, message1)
    message2 = "foo2"
    sign2 = lamport.sign(sk, message2)
    # print(bin(int(sha256(message.encode()).hexdigest(), 16))[::-1])
    # print([sign.values[j].hexdigest() for j in range(256)])
    print(lamport.vrfy(pk, sign1, message1))
    print(lamport.vrfy(pk, sign1, message2))
    print(lamport.vrfy(pk, sign2, message1))
    print(lamport.vrfy(pk, sign2, message2))

    # input_ = "foo" # input('Enter something: ')
    # print(input_.encode('utf-8'))
    # print(type(input_.encode('utf-8')))
    # print(sha256(input_.encode()))
    # print(int(sha256(input_.encode()).hexdigest(), 16))
    # s = ""
    # for i in range(10):
    #     s += str(sha256(input_.encode()).digest()[i])
    # print(s)
    # print(sha256(input_.encode('utf-8')).digest())
    # print(sha256(input_.encode('utf-8')).hexdigest())
    # print(int(sha256(input_.encode('utf-8')).hexdigest(), 16))
    # print(int(sha256(input_.encode('utf-8')).hexdigest(), 16) & 0b10)

    # a = 5.to_bytes()
    # b = bytes(9)
    # print(a, b)
    # print(a[0], b[0])
    # print(a[0] & b[0])
    # print(bytes([a[0] & b[0]]))

    _, a = lamport.gen()
    _, b = lamport.gen()
    print(a.hash().hexdigest())
    c = sha256(a.hash().digest())
    print(c.hexdigest())
    c = sha256(a.hash().digest())
    print(c.hexdigest())
    a = b
    c = sha256(a.hash().digest())
    print(c.hexdigest())