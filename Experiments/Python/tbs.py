from typing import List

from hash import HashFunction, Hash
from mts import MTS
from ots import OTS


class Node:
    def __init__(self, top, sk: OTS.SecKey, pk: OTS.PubKey):
        self.top = top
        self.left = None
        self.right = None

        self.sk = sk
        self.pk = pk
        self.sign = None

        # Only for leafs
        self.message = None


class SignNode:
    def __init__(self,
                 pk: OTS.PubKey,
                 lpk: OTS.PubKey,
                 rpk: OTS.PubKey,
                 sign: OTS.Sign):
        self.pk = pk
        self.lpk = lpk
        self.rpk = rpk
        self.sign = sign


class TBS(MTS):

    class SecKey(MTS.SecKey):
        def __init__(self, head: Node):
            self.head = head

    class PubKey(MTS.PubKey):
        def __init__(self, pk: OTS.PubKey):
            self.key = pk

        def hash(self) -> Hash:
            return self.key.hash()

    class Sign(MTS.Sign):
        def __init__(self, chaine_nodes: List[SignNode]):
            self.chaine_nodes = chaine_nodes

    def __init__(self, hashF: HashFunction, hash_size: int, ots: OTS):
        if not issubclass(self.__class__, MTS):
            raise NotImplementedError
        super().__init__(hashF, hash_size, ots)

    def gen(self) -> {SecKey, PubKey}:
        ots_sk, ots_pk = self.ots.gen()
        sk = self.SecKey(Node(None, ots_sk, None))
        pk = self.PubKey(ots_pk)
        return sk, pk

    def sign(self, sk: SecKey, message: str) -> Sign:
        msg = self.__msg_to_hash_int(message)
        cur = sk.head
        for i in range(self.hash_size):
            cur.left = self.__child_node_gen(cur)
            cur.right = self.__child_node_gen(cur)
            node_message = self.__conc(cur.left.pk, cur.right.pk)
            cur.sign = self.ots.sign(cur.sk, node_message)

            cur = cur.right if msg & ((msg >> i) & 0b1) else cur.left

        leaf = cur
        leaf.sign = self.ots.sign(leaf.sk, message)

        sign = self.Sign([SignNode(cur.pk, None, None, cur.sign)])
        cur = cur.top
        while cur is not None:
            sign.chaine_nodes.append(SignNode(cur.pk, cur.left.pk, cur.right.pk, cur.sign))
            cur = cur.top

        leaf.message = message

        return sign

    def vrfy(self, pk: PubKey, sign: Sign, message: str) -> bool:
        if not self.ots.vrfy(sign.chaine_nodes[0].pk, sign.chaine_nodes[0].sign, message):
            return False

        sign.chaine_nodes[-1].pk = pk.key
        msg = self.__msg_to_hash_int(message)
        for i in range(1, self.hash_size + 1):
            lower_pk = sign.chaine_nodes[i].rpk if msg & ((msg >> (self.hash_size - i)) & 0b1) else sign.chaine_nodes[i].lpk
            if lower_pk != sign.chaine_nodes[i - 1].pk:
                sign.chaine_nodes[-1].pk = None
                return False

            message = self.__conc(sign.chaine_nodes[i].lpk, sign.chaine_nodes[i].rpk)
            if not self.ots.vrfy(sign.chaine_nodes[i].pk, sign.chaine_nodes[i].sign, message):
                sign.chaine_nodes[-1].pk = None
                return False

        sign.chaine_nodes[-1].pk = None
        return True

    def __msg_to_hash_int(self, message: str):
        return int(self.hashF(message.encode()).hexdigest(), 16)

    def __child_node_gen(self, cur: Node):
        ots_sk, ots_pk = self.ots.gen()
        return Node(cur, ots_sk, ots_pk)

    def __conc(self, lpk: OTS.PubKey, rpk: OTS.PubKey):
        return lpk.hash().hexdigest() + " # " + rpk.hash().hexdigest()


if __name__ == "__main__":
    from hashlib import sha256

    from lamport import Lamport

    hashF = HashFunction(sha256)
    lamport = Lamport(hashF, 256, 123)
    tbs = TBS(hashF, 256, lamport)

    sk, pk = tbs.gen()
    msg1 = "foo1"
    msg2 = "foo2"
    msg3 = "foo3"

    sign1 = tbs.sign(sk, msg1)
    sign2 = tbs.sign(sk, msg2)
    sign3 = tbs.sign(sk, msg3)

    print(tbs.vrfy(pk, sign1, msg1), tbs.vrfy(pk, sign1, msg2), tbs.vrfy(pk, sign1, msg3))
    print(tbs.vrfy(pk, sign2, msg1), tbs.vrfy(pk, sign2, msg2), tbs.vrfy(pk, sign2, msg3))
    print(tbs.vrfy(pk, sign3, msg1), tbs.vrfy(pk, sign3, msg2), tbs.vrfy(pk, sign3, msg3))
