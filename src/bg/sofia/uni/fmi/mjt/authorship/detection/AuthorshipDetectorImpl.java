package bg.sofia.uni.fmi.mjt.authorship.detection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class AuthorshipDetectorImpl implements AuthorshipDetector {
    public static final int MAX = 5;
    public static final int VALUE_ONE = 1;
    public static final int MIN = 100000;
    public static final int VALUE_ZERO = 0;
    public static final int VALUE_TWO = 2;
    public static final int VALUE_THREE = 3;
    public static final int VALUE_FOUR = 4;
    public static final int CONST = 1;
    private double[] weights;
    HashMap<FeatureType, Double> map;
    HashMap<String, LinguisticSignature> author;

    void addFeature(int feature, double value) {
        map.put(FeatureType.valueOf(feature), value);
    }

    public AuthorshipDetectorImpl(InputStream signaturesDataset, double[] weights) {
        author = new HashMap<>();
        String line = "";
        String[] featureScores;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(signaturesDataset))) {
            while ((line = reader.readLine()) != null) {
                featureScores = line.split(",");
                map = new HashMap<>();
                for (int i = 0; i < MAX; i++) {
                    addFeature(i, Double.parseDouble(featureScores[i + VALUE_ONE]));
                }
                LinguisticSignature signature = new LinguisticSignature(map);
                author.put(featureScores[0], signature);
            }
            this.weights = weights;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LinguisticSignature calculateSignature(InputStream mysteryText) {
        if (mysteryText == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mysteryText))) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(" ");
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<FeatureType, Double> feature = new HashMap<>();
        feature.put(FeatureType.AVERAGE_WORD_LENGTH, averageLength(stringBuilder));
        feature.put(FeatureType.TYPE_TOKEN_RATIO, typeTokenRatio(stringBuilder));
        feature.put(FeatureType.HAPAX_LEGOMENA_RATIO, hapaxLegomenaRatio(stringBuilder));
        feature.put(FeatureType.AVERAGE_SENTENCE_LENGTH, averageWordsInSentence(stringBuilder));
        feature.put(FeatureType.AVERAGE_SENTENCE_COMPLEXITY, wordComplexity(stringBuilder));
        return new LinguisticSignature(feature);
    }

    @Override
    public double calculateSimilarity(LinguisticSignature firstSignature, LinguisticSignature secondSignature) {

        HashMap<FeatureType, Double> first = new HashMap<>(firstSignature.getFeatures());
        HashMap<FeatureType, Double> second = new HashMap<>(secondSignature.getFeatures());

        double sum = 0;
        sum = Math.abs(first.get(FeatureType.AVERAGE_WORD_LENGTH) - second.get(FeatureType.AVERAGE_WORD_LENGTH))
                * weights[VALUE_ZERO];
        sum += Math.abs(first.get(FeatureType.TYPE_TOKEN_RATIO) - second.get(FeatureType.TYPE_TOKEN_RATIO))
                * weights[VALUE_ONE];
        sum += Math.abs(first.get(FeatureType.HAPAX_LEGOMENA_RATIO) - second.get(FeatureType.HAPAX_LEGOMENA_RATIO))
                * weights[VALUE_TWO];
        sum += Math.abs(first.get(FeatureType.AVERAGE_SENTENCE_LENGTH) -
                second.get(FeatureType.AVERAGE_SENTENCE_LENGTH)) * weights[VALUE_THREE];
        sum += Math.abs(first.get(FeatureType.AVERAGE_SENTENCE_COMPLEXITY) -
                second.get(FeatureType.AVERAGE_SENTENCE_COMPLEXITY)) * weights[VALUE_FOUR];
        return sum;
    }

    @Override
    public String findAuthor(InputStream mysteryText) {
        double min = MIN;
        String name = "";
        LinguisticSignature signature = calculateSignature(mysteryText);
        for (Map.Entry<String, LinguisticSignature> entry : author.entrySet()) {
            if (min > calculateSimilarity(entry.getValue(), signature)) {
                min = calculateSimilarity(entry.getValue(), signature);
                name = entry.getKey();
            }
        }
        return name;
    }

    public static String cleanUp(String word) {
        return word.toLowerCase()
                .replaceAll("^[!.,:;\\-?<>#\\*\'\"\\[\\(\\]\\)\\n\\t\\\\]+|[!.,:;\\-?<>#\\*\'\"\\[" +
                        "\\(\\]\\)\\n\\t\\\\]+$", "");
    }

    public ArrayList<String> getList(StringBuilder stream) {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();

        String[] arr = stream.toString().split("\\s+");
        list.addAll(Arrays.asList(arr));

        for (String s : list) {
            newList.add(cleanUp(s));
        }
        return newList;
    }

    public double averageLength(StringBuilder stream) {


        ArrayList<String> list = getList(stream);
        long wordCount = list.stream()
                .filter(Objects::nonNull)
                .count();
        int symbolCount = 0;
        for (String s : list) {
            symbolCount += cleanUp(s).length();
        }
        return symbolCount / (double) wordCount;
    }

    public double typeTokenRatio(StringBuilder stream) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : getList(stream)) {
            list.add(cleanUp(s));
        }
        long count = list.stream()
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long countAll = list.stream()
                .filter(Objects::nonNull)
                .count();
        return count / (double) countAll;
    }

    public double hapaxLegomenaRatio(StringBuilder stream) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : getList(stream)) {
            list.add(cleanUp(s));
        }
        long countAll = list.stream()
                .filter(Objects::nonNull)
                .count();

        Map<String, Long> counts = list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        int result = 0;
        for (Map.Entry<String, Long> e : counts.entrySet()) {
            if (e.getValue() == CONST) {
                result++;
            }
        }
        return result / (double) countAll;
    }

    private ArrayList<String> getSentences(StringBuilder stream) {
        ArrayList<String> arrayList = new ArrayList<>();

        String[] sentences = stream.toString().split("[!?.]");
        for (int i = 0; i < sentences.length; i++) {
            if (sentences[i] != null) {
                arrayList.add(sentences[i].trim());
            }
        }
        return arrayList;

    }

    public double averageWordsInSentence(StringBuilder stream) {
        ArrayList<String> list = new ArrayList<>();
        list = getSentences(stream);
        long countWords = 0;
        for (String s : list) {
            String[] words = s.split(" ");
            countWords += words.length;

        }
        long sentCount = list.size();
        return countWords / (double) sentCount;
    }

    public double wordComplexity(StringBuilder stream) {
        ArrayList<String> list = getSentences(stream);
        int sentCount = list.size();
        int phraseCount = 0;
        for (String s : list) {
            phraseCount += s.split("[:;,]").length;
        }
        return phraseCount / (double) sentCount;
    }
}