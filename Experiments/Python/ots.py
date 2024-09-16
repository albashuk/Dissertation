import abc
from time import time

from hash import HashFunction
from signature import Signature


class OTS(Signature):

    def __init__(self, hashF: HashFunction, hash_size: int, seed_key: int):
        super().__init__(hashF, hash_size)
        self.seed_key = seed_key

    class PubKey(Signature.PubKey):
        @abc.abstractmethod
        def __eq__(self, other) -> bool:
            raise NotImplementedError

        @abc.abstractmethod
        def __ne__(self, other) -> bool:
            raise NotImplementedError

    def _seed(self):
        return int(time() * 1000) + self.seed_key

