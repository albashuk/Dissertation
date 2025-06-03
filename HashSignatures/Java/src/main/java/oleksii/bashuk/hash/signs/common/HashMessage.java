package oleksii.bashuk.hash.signs.common;

import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;
import oleksii.bashuk.hash.signs.signature.Signature;

public record HashMessage(Hash value) implements Signature.Message {}
