class Hash:
    def __init__(self, hash):
        self.hash = hash

    def __deepcopy__(self, memodict={}):
        return Hash(self.hash.copy())

    def __eq__(self, other) -> bool:
        return self.hash.digest() == other.hash.digest()

    def __ne__(self, other) -> bool:
        return not self.__eq__(other)

    def digest(self):
        return self.hash.digest()

    def hexdigest(self):
        return self.hash.hexdigest()

    def update(self, other):
        return self.hash.update(other)


class HashFunction:
    def __init__(self, hashF):
        self.hashF = hashF

    def __call__(self, *args, **kwargs) -> Hash:
        return Hash(self.hashF(*args, **kwargs))
