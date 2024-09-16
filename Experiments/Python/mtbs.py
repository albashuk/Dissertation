from typing import List

from hash import HashFunction, Hash
from mts import MTS
from ots import OTS


class Node:
    def __init__(self, top, sk: OTS.SecKey, pk: OTS.PubKey):
        self.top = top
        self.prev = None
        self.next = None
        self.left = None
        self.middle = None
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
                 mpk: OTS.PubKey,
                 rpk: OTS.PubKey,
                 sign: OTS.Sign):
        self.pk = pk
        self.lpk = lpk
        self.mpk = mpk
        self.rpk = rpk
        self.sign = sign


class MTBS(MTS):

    class SecKey(MTS.SecKey):
        def __init__(self, head: Node):
            self.head = head
            self.first = head
            self.cur = head

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
        sk.cur.left = self.__child_node_gen(sk.cur)
        sk.cur.middle = self.__child_node_gen(sk.cur)
        sk.cur.right = self.__child_node_gen(sk.cur)

        if sk.cur.prev is None:
            sk.first = sk.cur.left
        else:
            self.__connect_neigh_nodes(sk.cur.prev.right, sk.cur.left)
        self.__connect_neigh_nodes(sk.cur.left, sk.cur.right)

        node_message = self.__conc(sk.cur.left.pk, sk.cur.middle.pk, sk.cur.right.pk)
        sk.cur.sign = self.ots.sign(sk.cur.sk, node_message)
        sk.cur.middle.sign = self.ots.sign(sk.cur.middle.sk, message)

        sign = self.Sign([SignNode(sk.cur.middle.pk, None, None, None, sk.cur.middle.sign)])
        cur = sk.cur
        while cur is not None:
            sign.chaine_nodes.append(SignNode(cur.pk, cur.left.pk, cur.middle.pk, cur.right.pk, cur.sign))
            cur = cur.top

        if sk.cur.next is None:
            sk.cur = sk.first
        else:
            sk.cur = sk.cur.next

        return sign

    def vrfy(self, pk: PubKey, sign: Sign, message: str) -> bool:
        if not self.ots.vrfy(sign.chaine_nodes[0].pk, sign.chaine_nodes[0].sign, message):
            return False

        i = 1
        last_pk = sign.chaine_nodes[1].lpk
        sign.chaine_nodes[-1].pk = pk.key
        while i < len(sign.chaine_nodes):
            node = sign.chaine_nodes[i]

            if node.lpk != last_pk and node.rpk != last_pk:
                sign.chaine_nodes[-1].pk = None
                return False

            message = self.__conc(node.lpk, node.mpk, node.rpk)
            if not self.ots.vrfy(node.pk, node.sign, message):
                sign.chaine_nodes[-1].pk = None
                return False

            i += 1
            last_pk = node.pk

        sign.chaine_nodes[-1].pk = None
        return True

    def __child_node_gen(self, cur: Node):
        ots_sk, ots_pk = self.ots.gen()
        return Node(cur, ots_sk, ots_pk)

    def __conc(self, lpk: OTS.PubKey, mpk: OTS.PubKey, rpk: OTS.PubKey):
        return lpk.hash().hexdigest() + " # " + mpk.hash().hexdigest() + " # " + rpk.hash().hexdigest()

    @staticmethod
    def __connect_neigh_nodes(left: Node, right: Node):
        left.next = right
        right.prev = left


if __name__ == "__main__":
    from hashlib import sha256

    from lamport import Lamport

    hashF = HashFunction(sha256)
    lamport = Lamport(hashF, 256, 123)
    mtbs = MTBS(hashF, 256, lamport)

    sk, pk = mtbs.gen()
    msg1 = "foo1"
    msg2 = "foo2"
    msg3 = "foo3"

    sign1 = mtbs.sign(sk, msg1)
    sign2 = mtbs.sign(sk, msg2)
    sign3 = mtbs.sign(sk, msg3)
    sign4 = mtbs.sign(sk, msg1)
    sign5 = mtbs.sign(sk, msg2)
    sign6 = mtbs.sign(sk, msg3)
    sign7 = mtbs.sign(sk, msg1)
    sign8 = mtbs.sign(sk, msg2)
    sign9 = mtbs.sign(sk, msg3)

    print(mtbs.vrfy(pk, sign1, msg1), mtbs.vrfy(pk, sign1, msg2), mtbs.vrfy(pk, sign1, msg3))
    print(mtbs.vrfy(pk, sign2, msg1), mtbs.vrfy(pk, sign2, msg2), mtbs.vrfy(pk, sign2, msg3))
    print(mtbs.vrfy(pk, sign3, msg1), mtbs.vrfy(pk, sign3, msg2), mtbs.vrfy(pk, sign3, msg3))
    print(mtbs.vrfy(pk, sign4, msg1))
    print(mtbs.vrfy(pk, sign5, msg2))
    print(mtbs.vrfy(pk, sign6, msg3))
    print(mtbs.vrfy(pk, sign7, msg1))
    print(mtbs.vrfy(pk, sign8, msg2))
    print(mtbs.vrfy(pk, sign9, msg3))