import abc

from hash import HashFunction, Hash


class Signature(metaclass=abc.ABCMeta):

    def __init__(self, hashF: HashFunction, hash_size: int):
        self.hashF = hashF
        self.hash_size = hash_size

    @classmethod
    def __subclasshook__(cls, subclass):
        return (hasattr(subclass, 'SecKey') and
                issubclass(subclass.SecKey, cls.SecKey) and
                subclass.SecKey != cls.SecKey and
                hasattr(subclass, 'PubKey') and
                issubclass(subclass.PubKey, cls.PubKey) and
                subclass.PubKey != cls.PubKey and
                hasattr(subclass, 'Sign') and
                issubclass(subclass.Sign, cls.Sign) and
                subclass.Sign != cls.Sign and
                hasattr(subclass, 'gen') and
                callable(subclass.gen) and
                hasattr(subclass, 'sign') and
                callable(subclass.sign) and
                hasattr(subclass, 'vrfy') and
                callable(subclass.vrfy))

    class SecKey(metaclass=abc.ABCMeta):
        pass

    class PubKey(metaclass=abc.ABCMeta):
        @abc.abstractmethod
        def hash(self) -> Hash:
            raise NotImplementedError

    class Sign(metaclass=abc.ABCMeta):
        pass

    @abc.abstractmethod
    def gen(self) -> {SecKey, PubKey}:
        raise NotImplementedError

    @abc.abstractmethod
    def sign(self, sk: SecKey, message: str) -> Sign:
        raise NotImplementedError

    @abc.abstractmethod
    def vrfy(self, pk: PubKey, sign: Sign, message: str) -> bool:
        raise NotImplementedError
