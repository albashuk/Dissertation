package oleksii.bashuk.hash.signs.realisation.gwots.communication;

import oleksii.bashuk.hash.signs.hash.HashFunction.Hash;

public record BRequestPlus(int t, int index, int indexB, byte[] msg, Hash conf) { }
