from typing import List

from hash import HashFunction, Hash
from mts import MTS
from ots import OTS


class CBS(MTS):

    class SecKey(MTS.SecKey):
        def __init__(self,
                     chaine_sks: List[OTS.SecKey],
                     chaine_pks: List[OTS.PubKey],
                     chaine_signs: List[OTS.Sign],
                     chaine_msgs: List[any]):
            self.chaine_sks = chaine_sks
            self.chaine_pks = chaine_pks
            self.chaine_signs = chaine_signs
            self.chaine_msgs = chaine_msgs

    class PubKey(MTS.PubKey):
        def __init__(self, pk: OTS.PubKey):
            self.key = pk

        def hash(self) -> Hash:
            return self.key.hash()

    class Sign(MTS.Sign):
        def __init__(self,
                     chaine_pks: List[OTS.PubKey],
                     chaine_signs: List[OTS.Sign],
                     chaine_msgs: List[any]):
            self.chaine_pks = chaine_pks
            self.chaine_signs = chaine_signs
            self.chaine_msgs = chaine_msgs

    def __init__(self, hashF: HashFunction, hash_size: int, ots: OTS):
        if not issubclass(self.__class__, MTS):
            raise NotImplementedError
        super().__init__(hashF, hash_size, ots)

    def gen(self) -> {SecKey, PubKey}:
        ots_sk, ots_pk = self.ots.gen()
        sk = self.SecKey([ots_sk], [None], [], [])
        pk = self.PubKey(ots_pk)
        return sk, pk

    def sign(self, sk: SecKey, message: str) -> Sign:
        new_ots_sk, new_ots_pk = self.ots.gen()
        _message = self.__conc(message, new_ots_pk)
        ots_sign = self.ots.sign(sk.chaine_sks[-1], _message)

        sk.chaine_sks.append(new_ots_sk)
        sk.chaine_pks.append(new_ots_pk)
        sk.chaine_signs.append(ots_sign)
        sk.chaine_msgs.append(None)

        sign = self.Sign(sk.chaine_pks[:], sk.chaine_signs[:], sk.chaine_msgs[:])

        sk.chaine_msgs[-1] = message

        return sign

    def vrfy(self, pk: PubKey, sign: Sign, message: str) -> bool:
        sign.chaine_pks[0] = pk.key
        sign.chaine_msgs[-1] = message
        for i in range(len(sign.chaine_signs)):
            message = self.__conc(sign.chaine_msgs[i], sign.chaine_pks[i + 1])
            if not self.ots.vrfy(sign.chaine_pks[i], sign.chaine_signs[i], message):
                sign.chaine_pks[0] = None
                sign.chaine_msgs[-1] = None
                return False
        sign.chaine_pks[0] = None
        sign.chaine_msgs[-1] = None
        return True

    def __conc(self, message: str, pk: OTS.PubKey):
        return message + " # " + pk.hash().hexdigest()


if __name__ == "__main__":
    from hashlib import sha256

    from lamport import Lamport

    hashF = HashFunction(sha256)
    lamport = Lamport(hashF, 256, 123)
    cbs = CBS(hashF, 256, lamport)

    sk, pk = cbs.gen()
    msg1 = "foo1"
    msg2 = "foo2"
    msg3 = "foo3"

    sign1 = cbs.sign(sk, msg1)
    sign2 = cbs.sign(sk, msg2)
    sign3 = cbs.sign(sk, msg3)

    print(cbs.vrfy(pk, sign1, msg1), cbs.vrfy(pk, sign1, msg2), cbs.vrfy(pk, sign1, msg3))
    print(cbs.vrfy(pk, sign2, msg1), cbs.vrfy(pk, sign2, msg2), cbs.vrfy(pk, sign2, msg3))
    print(cbs.vrfy(pk, sign3, msg1), cbs.vrfy(pk, sign3, msg2), cbs.vrfy(pk, sign3, msg3))
