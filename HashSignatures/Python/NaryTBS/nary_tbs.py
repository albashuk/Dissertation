import pathlib

from typing import List

from hash import HashFunction, Hash
from mts import MTS
from mts_measures import MTSMeasures
from ots import OTS
from time import time
from pympler import asizeof
from datetime import datetime


class Node:
    def __init__(self, parent, sk: OTS.SecKey, pk: OTS.PubKey):
        self.parent = parent
        self.previous: Node = None
        self.next: Node = None
        self.children: List[Node] = []

        self.sk = sk
        self.pk = pk
        self.sign: OTS.Sign = None
        self.msg: str = None


class NaryTBS(MTS):
    class SecKey(MTS.SecKey):
        def __init__(self, head: Node):
            self.head = head
            self.last = head
            self.next_for_sign = head
            self.size = 0

    class PubKey(MTS.PubKey):
        def __init__(self, head_pk: OTS.PubKey):
            self.head_pk = head_pk

        def hash(self) -> Hash:
            return self.head_pk.hash()

    class Sign(MTS.Sign):
        def __init__(self, chaine_nodes: List[Node]):
            self.chaine_nodes = chaine_nodes

    def __init__(self, hashF: HashFunction, hash_size: int, ots: OTS, arity: int, use_proxy_node: bool, measuring: bool):
        if not issubclass(self.__class__, MTS):
            raise NotImplementedError
        super().__init__(hashF, hash_size, ots)
        self.arity = arity
        self.use_proxy_node = use_proxy_node
        self.measuring = measuring
        self.measures = MTSMeasures(hashF.name, arity, use_proxy_node)

    def gen(self) -> {SecKey, PubKey}:
        ots_sk, ots_pk = self.ots.gen()
        sk = self.SecKey(Node(None, ots_sk, None))
        pk = self.PubKey(ots_pk)
        return sk, pk

    def sign(self, sk: SecKey, msg: str) -> Sign:
        if self.measuring:
            timeStamp = time()

        sign_node = self.__sign_by_next_node(sk, msg)
        sk.next_for_sign = sk.next_for_sign.next
        sk.size += 1

        if self.measuring:
            self.measures.sign_time.append(time() - timeStamp)
            timeStamp = time()

        sign = self.__create_signature_chain(sk, sign_node)

        if self.measuring:
            self.measures.sign_creation_time.append(time() - timeStamp)
            if sk.size % 10 == 0:
                self.measures.sign_size.append(asizeof.asizeof(sign))

        return sign

    def __sign_by_next_node(self, sk: SecKey, msg: str) -> Node:
        cur_node = sk.next_for_sign
        for i in range(self.arity):
            ots_sk, ots_pk = self.ots.gen()
            cur_node.children.append(Node(cur_node, ots_sk, ots_pk))

        # Connect newly generated nodes
        cur_node.children[0].previous = sk.last
        sk.last.next = cur_node.children[0]
        sk.last = cur_node.children[-1]
        for i in range(1, self.arity):
            cur_node.children[i].previous = cur_node.children[i - 1]
            cur_node.children[i - 1].next = cur_node.children[i]

        # Generate proxy node if needed, and sign the msg by the proxy node
        if self.use_proxy_node:
            ots_sk, ots_pk = self.ots.gen()
            cur_node.children.append(Node(cur_node, ots_sk, ots_pk))
            cur_node.children[self.arity].msg = msg
            cur_node.children[self.arity].sign = self.ots.sign(cur_node.children[self.arity].sk, msg)
        else:
            # Otherwise, just add msg to the node
            cur_node.msg = msg

        # Create common hash of child nodes (their pub keys)
        _hash = self.__prepare_hash_for_sign(cur_node)
        cur_node.sign = self.ots.sign(cur_node.sk, _hash.hexdigest())
        return cur_node

    def __sign_in_full_tree(self, sk: SecKey, msg: str) -> Node:
        msg_ind = self.hashF(msg.encode()).hexdigest()
        highest_ind = (1 << self.hash_size) - 1
        cur_node = sk.head
        while highest_ind > 0:
            if len(cur_node.children) == 0:
                for i in range(self.arity):
                    ots_sk, ots_pk = self.ots.gen()
                    cur_node.children.append(Node(cur_node, ots_sk, ots_pk))
                _hash = self.__prepare_hash_for_sign(cur_node)
                cur_node.sign = self.ots.sign(cur_node.sk, _hash.hexdigest())

            child_ind = msg_ind % self.arity
            cur_node = cur_node.children[child_ind]
            msg_ind //= self.arity
            highest_ind //= self.arity

        cur_node.msg = msg
        _hash = self.__prepare_hash_for_sign(cur_node)
        cur_node.sign = self.ots.sign(cur_node.sk, _hash.hexdigest())
        return cur_node

    def __create_signature_chain(self, sk: SecKey, start_node: Node) -> Sign:
        cur_node = start_node
        sign = self.Sign([])
        last_cur_node: Node = None
        last_sign_node: Node = None
        while cur_node is not None:
            sign_node = Node(None, None, None)
            sign_node.sign = cur_node.sign.copy()
            sign_node.pk = cur_node.pk.copy() if cur_node.parent is not None else None
            if not self.use_proxy_node:
                sign_node.msg = cur_node.msg
            for child in cur_node.children:
                if child == last_cur_node:
                    last_sign_node.parent = sign_node
                    sign_node.children.append(last_sign_node)
                else:
                    sign_child = Node(sign_node, None, child.pk.copy())
                    if self.use_proxy_node and cur_node == start_node and child.previous is None:
                        sign_child.sign = child.sign.copy()
                        sign_child.msg = child.msg
                    sign_node.children.append(sign_child)
            sign.chaine_nodes.append(sign_node)
            last_cur_node = cur_node
            last_sign_node = sign_node
            cur_node = cur_node.parent
        return sign

    def vrfy(self, pk: PubKey, sign: Sign, msg: str) -> bool:
        if self.measuring:
            timeStamp = time()

        cur_node = sign.chaine_nodes[0]
        if msg != (cur_node.children[self.arity].msg if self.use_proxy_node else cur_node.msg):
            return False
        sign.chaine_nodes[-1].pk = pk.head_pk

        if self.use_proxy_node:
            proxy_node = cur_node.children[self.arity]
            if not self.ots.vrfy(proxy_node.pk, proxy_node.sign, proxy_node.msg):
                sign.chaine_nodes[-1].pk = None
                return False

        last_node = cur_node.children[0]
        while cur_node is not None:
            last_node_is_child = False
            for child in cur_node.children:
                if child == last_node:
                    last_node_is_child = True
                    break
            if not last_node_is_child:
                sign.chaine_nodes[-1].pk = None
                return False

            _hash = self.__prepare_hash_for_sign(cur_node)
            if not self.ots.vrfy(cur_node.pk, cur_node.sign, _hash.hexdigest()):
                sign.chaine_nodes[-1].pk = None
                return False

            last_node = cur_node
            cur_node = cur_node.parent

        sign.chaine_nodes[-1].pk = None

        if self.measuring:
            self.measures.vrfy_time.append(time() - timeStamp)

        return True

    def __prepare_hash_for_sign(self, node: Node):
        # Create common hash of child nodes (their pub keys)
        _hash = self.hashF()
        for child in node.children:
            _hash.update(child.pk.hash().digest())

        if node.msg is not None: # not self.use_proxy_node:
            # Otherwise, concat common hash with the msg hash
            _hash.update(node.msg.encode())
        return _hash




if __name__ == "__main__":
    from hashlib import sha256
    from lamport import Lamport

    seed_key = 123
    iter = 100000
    testing = False
    iterating = True
    measuring = True
    arity = 3
    use_proxy_node = 0
    full_tree = 0
    use_proxy_node_bool = True if use_proxy_node != 0 else False
    full_tree_bool = True if full_tree != 0 else False

    sha256('123'.encode('utf-8')).copy()
    hashF = HashFunction(sha256)
    lamport = Lamport(hashF, 256, seed_key)
    nary_tbs = NaryTBS(hashF, 256, lamport, arity, use_proxy_node_bool, measuring)
    sk, pk = nary_tbs.gen()

    if testing:
        msg1 = "foo1"
        msg2 = "foo2"
        msg3 = "foo3"

        sign1 = nary_tbs.sign(sk, msg1)
        sign2 = nary_tbs.sign(sk, msg2)
        sign3 = nary_tbs.sign(sk, msg3)

        print(nary_tbs.vrfy(pk, sign1, msg1), nary_tbs.vrfy(pk, sign1, msg2), nary_tbs.vrfy(pk, sign1, msg3))
        print(nary_tbs.vrfy(pk, sign2, msg1), nary_tbs.vrfy(pk, sign2, msg2), nary_tbs.vrfy(pk, sign2, msg3))
        print(nary_tbs.vrfy(pk, sign3, msg1), nary_tbs.vrfy(pk, sign3, msg2), nary_tbs.vrfy(pk, sign3, msg3))

    if iterating:
        try:
            flag = False
            for i in range(iter):
                t = time()
                with open("interrupt.txt", 'r') as datafile:
                    for line in datafile:
                        if len(line) > 0:
                            flag = True
                            break
                if flag:
                    print("\r", i)
                    break

                msg = str(i)
                sign = nary_tbs.sign(sk, msg)
                if time() - t > 1:
                    print("\r", i)
                if not nary_tbs.vrfy(pk, sign, msg):
                    print("\r", i)
                if i % 100 == 0:
                    print("\r", 100*i/iter, "%", sep="", end="")
        finally:
            if measuring:
                path = 'data/' + nary_tbs.__class__.__name__ + '/' + str(arity) + '_' + str(use_proxy_node) + '_' + str(full_tree) + '/'
                filename = datetime.now().strftime("%y-%m-%d-%H-%M-%S") + '.txt'
                pathlib.Path(path).mkdir(parents=True, exist_ok=True)
                with open(path + filename, 'w') as file:
                    file.write(str(nary_tbs.measures.sign_time) + "\n")
                    file.write(str(nary_tbs.measures.sign_creation_time) + "\n")
                    file.write(str(nary_tbs.measures.vrfy_time) + "\n")
                    file.write(str(nary_tbs.measures.sign_size) + "\n")


