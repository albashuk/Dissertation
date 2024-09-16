from hash import HashFunction
from ots import OTS
from signature import Signature


class MTS(Signature):

    def __init__(self, hashF: HashFunction, hash_size: int, ots: OTS):
        super().__init__(hashF, hash_size)
        self.ots = ots

