package bg.sofia.uni.fmi.mjt.authorship.detection;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class AuthorshipDetectorTest {
    public static final int VALUE_ONE = 33;
    public static final int VALUE_ZERO = 11;
    public static final int VALUE_TWO = 50;
    public static final double VALUE_THREE = 0.4;
    public static final int VALUE_FOUR = 4;
    public static final double CONST_ZERO = 5.4;
    public static final double CONST_ONE = 1.0;
    public static final double CONST_TWO = 1.0;
    public static final double CONST_THREE = 10.0;
    public static final double CONST_FOUR = 5.0;
    public static final double AL_CONST = 5.4;
    public static final double TTR_CONST = 1.0;
    public static final double HLPR_CONST = 1.0;
    public static final double AWS_CONST = 10.0;
    public static final double WC_CONST = 5.0;
    private LinguisticSignature signature;
    private static String text = "Emma Woodhouse, handsome, clever, and rich, with a comfortable home";
    private StringBuilder stringBuilder = new StringBuilder(text);

    private static String authors = "Agatha Christie, 5.4, 1.0, 1.0, 10.0, 5.0\n" +
            "Alexandre Dumas, 4.38235547477, 0.049677588873, 0.0212183996175, 15.0054854981, 2.63499369483\n" +
            "Brothers Grim, 3.96868608302, 0.0529378997714, 0.0208217283571, 22.2267197987, 3.4129614094\n" +
            "Charles Dickens, 4.34760725241, 0.0803220950584, 0.0390662700499, 16.2613453121, 2.87721723105\n" +
            "Douglas Adams, 4.33408042189, 0.238435104414, 0.141554321967, 13.2874354561, 1.86574870912\n" +
            "Fyodor Dostoevsky, 4.34066732195, 0.0528571428571, 0.0233414043584, 12.8108273249, 2.16705364781\n" +
            "James Joyce, 4.52346300961, 0.120109917189, 0.0682315429476, 10.9663296918, 1.79667373227\n" +
            "Jane Austen, 4.41553119311, 0.0563451817574, 0.02229943808, 16.8869087498, 2.54817097682\n" +
            "Lewis Carroll, 4.22709528497, 0.111591342227, 0.0537026953444, 16.2728740581, 2.86275565124\n" +
            "Mark Twain, 4.33272222298, 0.117254215021, 0.0633074228159, 14.3548573631, 2.43716268311\n" +
            "Sir Arthur Conan Doyle, 4.16808311494, 0.0822989796874, 0.0394458485444, 14.717564466, 2.2220872148\n" +
            "William Shakespeare, 4.16216957834, 0.105602561171, 0.0575348730848, 9.34707371975, 2.24620146314";

    private InputStream textStream = new ByteArrayInputStream(text.getBytes());
    private InputStream authorsStream = new ByteArrayInputStream(authors.getBytes());
    private double[] weights = {VALUE_ZERO, VALUE_ONE, VALUE_TWO, VALUE_THREE, VALUE_FOUR};
    private AuthorshipDetectorImpl authorshipDetector = new AuthorshipDetectorImpl(authorsStream, weights);


    @Test
    public void calculateSignatureTest() {
        LinguisticSignature linguisticSignature = new LinguisticSignature(authorshipDetector
                .calculateSignature(textStream).getFeatures());

        HashMap<FeatureType, Double> feature = new HashMap<>();
        feature.put(FeatureType.AVERAGE_WORD_LENGTH, authorshipDetector.averageLength(stringBuilder));
        feature.put(FeatureType.TYPE_TOKEN_RATIO, authorshipDetector.typeTokenRatio(stringBuilder));
        feature.put(FeatureType.HAPAX_LEGOMENA_RATIO, authorshipDetector.hapaxLegomenaRatio(stringBuilder));
        feature.put(FeatureType.AVERAGE_SENTENCE_LENGTH, authorshipDetector.averageWordsInSentence(stringBuilder));
        feature.put(FeatureType.AVERAGE_SENTENCE_COMPLEXITY, authorshipDetector.wordComplexity(stringBuilder));
        LinguisticSignature signature = new LinguisticSignature(feature);

        assertEquals(signature.getFeatures().get(FeatureType.AVERAGE_WORD_LENGTH), linguisticSignature.getFeatures()
                .get(FeatureType.AVERAGE_WORD_LENGTH));
        assertEquals(signature.getFeatures().get(FeatureType.TYPE_TOKEN_RATIO), linguisticSignature.getFeatures()
                .get(FeatureType.TYPE_TOKEN_RATIO));
        assertEquals(signature.getFeatures().get(FeatureType.HAPAX_LEGOMENA_RATIO), linguisticSignature.getFeatures()
                .get(FeatureType.HAPAX_LEGOMENA_RATIO));
        assertEquals(signature.getFeatures().get(FeatureType.AVERAGE_SENTENCE_LENGTH), linguisticSignature.getFeatures()
                .get(FeatureType.AVERAGE_SENTENCE_LENGTH));
        assertEquals(signature.getFeatures().get(FeatureType.AVERAGE_SENTENCE_COMPLEXITY), linguisticSignature
                .getFeatures().get(FeatureType.AVERAGE_SENTENCE_COMPLEXITY));
    }

    @Test
    public void calculateSimilarityTest() {

        HashMap<FeatureType, Double> feature = new HashMap<>();
        feature.put(FeatureType.AVERAGE_WORD_LENGTH, authorshipDetector.averageLength(stringBuilder));
        feature.put(FeatureType.TYPE_TOKEN_RATIO, authorshipDetector.typeTokenRatio(stringBuilder));
        feature.put(FeatureType.HAPAX_LEGOMENA_RATIO, authorshipDetector.hapaxLegomenaRatio(stringBuilder));
        feature.put(FeatureType.AVERAGE_SENTENCE_LENGTH, authorshipDetector.averageWordsInSentence(stringBuilder));
        feature.put(FeatureType.AVERAGE_SENTENCE_COMPLEXITY, authorshipDetector.wordComplexity(stringBuilder));
        LinguisticSignature signature1 = new LinguisticSignature(feature);

        HashMap<FeatureType, Double> featureSecond = new HashMap<>();
        featureSecond.put(FeatureType.AVERAGE_WORD_LENGTH, CONST_ZERO);
        featureSecond.put(FeatureType.TYPE_TOKEN_RATIO, CONST_ONE);
        featureSecond.put(FeatureType.HAPAX_LEGOMENA_RATIO, CONST_TWO);
        featureSecond.put(FeatureType.AVERAGE_SENTENCE_LENGTH, CONST_THREE);
        featureSecond.put(FeatureType.AVERAGE_SENTENCE_COMPLEXITY, CONST_FOUR);
        LinguisticSignature signature2 = new LinguisticSignature(feature);
        Double expected = 0.0;
        Double actual = authorshipDetector.calculateSimilarity(signature1, signature2);

        assertEquals(expected, actual);
    }

    @Test
    public void findAuthorTest() {
        String expected = "Agatha Christie";
        String actual = authorshipDetector.findAuthor(textStream);
        assertEquals(expected, actual);

    }

    @Test
    public void averageLengthTest() {

        Double expected = AL_CONST;
        Double actual = authorshipDetector.averageLength(stringBuilder);

        assertEquals(expected, actual);

    }

    @Test
    public void typeTokenRatioTest() {
        Double expected = TTR_CONST;
        Double actual = authorshipDetector.typeTokenRatio(stringBuilder);

        assertEquals(expected, actual);
    }

    @Test
    public void hapaxLegomenaRatioTest() {
        Double expected = HLPR_CONST;
        Double actual = authorshipDetector.hapaxLegomenaRatio(stringBuilder);

        assertEquals(expected, actual);
    }

    @Test
    public void averageWordsInSentenceTest() {
        Double expected = AWS_CONST;
        Double actual = authorshipDetector.averageWordsInSentence(stringBuilder);

        assertEquals(expected, actual);
    }

    @Test
    public void wordComplexityTest() {
        Double expected = WC_CONST;
        Double actual = authorshipDetector.wordComplexity(stringBuilder);

        assertEquals(expected, actual);
    }
}
