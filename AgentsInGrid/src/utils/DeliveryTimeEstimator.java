package utils;

import java.util.List;

public class DeliveryTimeEstimator {
    public static double estimateDeliveryTime(List<Integer> pastDeliveryTimes) {
        int sum = 0;
        for (int time : pastDeliveryTimes) {
            sum += time;
        }
        return (double) sum / pastDeliveryTimes.size();
    }
}
