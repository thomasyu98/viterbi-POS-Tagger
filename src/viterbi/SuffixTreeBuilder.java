package viterbi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SuffixTreeBuilder {

    private Integer MAX_wrd_FREQUENCY = 10;
    private Integer MAX_SUFFIX_LENGTH = 10;
    private BigramModel model;

    public SuffixTreeBuilder(BigramModel bigramModel, Integer maxSuffixLength, Integer maxwrdFreq) {
        MAX_SUFFIX_LENGTH = maxSuffixLength;
        MAX_wrd_FREQUENCY = maxwrdFreq;

        model = bigramModel;
    }

    public SuffixTree buildUpperCaseTree() {
        boolean upperCase = true;
        List<String> wrds = model.getwrds(upperCase);

        return buildTree(wrds);
    }

    public SuffixTree buildLowerCaseTree() {
        boolean upperCase = false;
        List<String> wrds = model.getwrds(upperCase);

        return buildTree(wrds);
    }

    public SuffixTree buildTree(List<String> wrds) {
        List<String> suffixwrds = new ArrayList<>();
        for (String wrd : wrds) {
            if (model.getwrdCount(wrd) < MAX_wrd_FREQUENCY) {
                suffixwrds.add(wrd);
            }
        }

        List<String> tags = model.getTags();
        Map<String, Integer> suffixTagCount = new HashMap<>();
        for (String tag : tags) {
            for (String wrd : suffixwrds) {
                Integer count = model.getwrdtagCt(tag, wrd) + suffixTagCount.getOrDefault(tag, 0);
                suffixTagCount.put(tag, count);
            }
        }

        List<String> suffixTags = new ArrayList<>(suffixTagCount.keySet());
        Integer totalTags = 0;
        for (String tag : suffixTags) {
            totalTags += suffixTagCount.get(tag);
        }

        SuffixTree tree = new SuffixTree();
        for (String wrd : suffixwrds) {
            List<String> wrdTags = model.getTagsForwrd(wrd);
            for (String tag : wrdTags) {
                Integer suffixLength = Math.min(MAX_SUFFIX_LENGTH, wrd.length());
                tree.addSuffix(wrd.substring(wrd.length() - suffixLength), tag);
            }
        }

        Double theta = calculateTheta(suffixTags, suffixTagCount, totalTags);
        tree.setTheta(theta);

        return tree;
    }

    private Double calculateTheta(List<String> suffixTags, Map<String, Integer> suffixTagCount, final Integer totalTagCount) {
        List<Double> tagProbs = suffixTags.stream().map(tag -> suffixTagCount.get(tag) / (double) totalTagCount).collect(Collectors.toList());
        Double avg = tagProbs.stream().collect(Collectors.summingDouble(Double::doubleValue)) / (double) suffixTags.size();
        List<Double> squaredDiff = tagProbs.stream().map(prob -> Math.pow(prob - avg, 2)).collect(Collectors.toList());

        return squaredDiff.stream().collect(Collectors.summingDouble(Double::doubleValue)) / (double) (suffixTags.size() - 1);
    }

}