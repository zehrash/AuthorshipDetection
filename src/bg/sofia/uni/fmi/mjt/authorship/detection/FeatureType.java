package bg.sofia.uni.fmi.mjt.authorship.detection;

import java.util.HashMap;
import java.util.Map;

public enum FeatureType {
    AVERAGE_WORD_LENGTH(Constants.VALUE_ZERO),
    TYPE_TOKEN_RATIO(Constants.VALUE_ONE),
    HAPAX_LEGOMENA_RATIO(Constants.VALUE_TWO),
    AVERAGE_SENTENCE_LENGTH(Constants.VALUE_THREE),
    AVERAGE_SENTENCE_COMPLEXITY(Constants.VALUE_FOUR);


    private int value;
    private static Map<Integer, FeatureType> enums = new HashMap<>();

    private FeatureType(int value) {
        this.value = value;
    }

    static {
        for (FeatureType featureType : FeatureType.values()) {
            assert enums != null;
            enums.put(featureType.value, featureType);
        }
    }

    public static FeatureType valueOf(int feature) {
        return enums.get(feature);
    }

    public int getValue() {
        return value;
    }

    private static class Constants {
        public static final int VALUE_ZERO = 0;
        public static final int VALUE_ONE = 1;
        public static final int VALUE_TWO = 2;
        public static final int VALUE_THREE = 3;
        public static final int VALUE_FOUR = 4;
    }
}

