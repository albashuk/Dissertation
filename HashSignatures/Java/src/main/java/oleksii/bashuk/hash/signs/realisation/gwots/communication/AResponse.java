package oleksii.bashuk.hash.signs.realisation.gwots.communication;

import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS.BlockWOTSSign;
import oleksii.bashuk.hash.signs.signature.wots.BlockWOTS.BlockWOTSPubKey;

public record AResponse(int index, BlockWOTSSign sign, BlockWOTSPubKey hashedNewPubKey) {}
