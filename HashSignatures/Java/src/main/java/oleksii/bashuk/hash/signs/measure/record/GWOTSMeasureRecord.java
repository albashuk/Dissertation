package oleksii.bashuk.hash.signs.measure.record;

import java.text.DecimalFormat;
import java.util.List;
import java.util.StringJoiner;

public record GWOTSMeasureRecord(double buildRequest, List<Double> responses, double checkResponses) implements MeasureRecord {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.000000000");

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(" ");
        result.add(decimalFormat.format(buildRequest));
        for (double response: responses) {
            result.add(decimalFormat.format(response));
        }
        result.add(decimalFormat.format(checkResponses));
        return result.toString();
    }
}
