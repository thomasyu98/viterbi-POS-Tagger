package viterbi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BigramModel {

    private Integer MAX_SUFFIX_LENGTH;
    private Integer senCt;
    private Integer totalTagCt;
    
    private Map<String, Integer> tagCt;
    private Map<String, Map<String, Integer>> tagwrdCount;
    
    private Map<String, Integer> tagStartCount;
    private Map<String, Map<String, Integer>> tagTransCt;

    private Map<String, Integer> wrdCount;
    private Map<String, Map<String, Integer>> wrdtagCt;

    public BigramModel(Integer maxSuffixLength) {
        MAX_SUFFIX_LENGTH = maxSuffixLength;

        tagCt = new HashMap<>();
        wrdCount = new HashMap<>();
        wrdtagCt = new HashMap<>();
        tagStartCount = new HashMap<>();
        tagTransCt = new HashMap<>();
        tagwrdCount = new HashMap<>();

        
        senCt = 0;
        totalTagCt = 0;
    }

    public void train(File file) {
        try {
            Scanner sc = new Scanner(file);
            String prevTag = "";

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.isEmpty()) {
                    prevTag = "";
                    continue;
                }

                String[] wrdTag = line.split("\t");
                String wrd = wrdTag[0];
                String tag = wrdTag[1];

                incrementwrdCount(wrd);
                incrementtagCt(tag);
                incrementTagwrdCount(tag, wrd);
                if (prevTag != null && !prevTag.isEmpty()) {
                    incrementTagTansitionCount(prevTag, tag);
                } else if (prevTag.isEmpty()) {
                    incrementTagStartCount(tag);
                }
                prevTag = tag;
            }

            sc.close();
        } catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    public EvaluationResult evaluate(SuffixTree upperCaseTree, SuffixTree lowerCaseTree, File wrds, String outputFilename) {
        List<List<String>> sentenceTags = new ArrayList<>();
        List<List<String>> sentences = new ArrayList<>();
        try {
            Scanner sc = new Scanner(wrds);
            Viterbi viterbi = new Viterbi(this, upperCaseTree, lowerCaseTree, MAX_SUFFIX_LENGTH);

            List<String> currentSentence = new ArrayList<>();
            while (sc.hasNextLine()) {
                String wrd = sc.nextLine();

                if (wrd.isEmpty()) {
                    sentenceTags.add(viterbi.run(currentSentence));
                    sentences.add(currentSentence);
                    currentSentence = new ArrayList<>();
                } else {
                    currentSentence.add(wrd);
                }
            }

            if (currentSentence.size() > 0) {
                sentenceTags.add(viterbi.run(currentSentence));
                sentences.add(currentSentence);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(1);
        }

        return new EvaluationResult(sentences, sentenceTags);
    }

    public List<String> getwrds(boolean upperCase) {
    	
        List<String> wrds = new ArrayList<>(wrdCount.keySet());
        
        // strings with first character capitalized
        if (upperCase) { 
            wrds = wrds.stream().filter(wrd -> Character.isUpperCase(wrd.charAt(0))).collect(Collectors.toList());
        } 
        
        // everything else
        else { 
            wrds = wrds.stream().filter(wrd -> !Character.isUpperCase(wrd.charAt(0))).collect(Collectors.toList());
        }
        return wrds;
    }

    public Integer getwrdCount(String wrd) {
        return wrdCount.getOrDefault(wrd, 0);
    }

    public void incrementwrdCount(String wrd) {
        Integer count = wrdCount.getOrDefault(wrd, 0) + 1;
        wrdCount.put(wrd, count);
    }

    public Integer getTagStartCount(String tag) {
        return tagStartCount.getOrDefault(tag, 0);
    }

    public List<String> getTagsForwrd(String wrd) {
        return new ArrayList<>(wrdtagCt.getOrDefault(wrd, new HashMap<>()).keySet());
    }

    public void incrementTagStartCount(String tag) {
        Integer count = tagStartCount.getOrDefault(tag, 0) + 1;
        tagStartCount.put(tag, count);
        senCt++;
    }

    public List<String> getTags() {
        List<String> tags = new ArrayList<>(tagCt.keySet());

        return tags;
    }

    public Integer gettagCt(String tag) {
        return tagCt.getOrDefault(tag, 0);
    }

    public void incrementtagCt(String tag) {
        Integer count = tagCt.getOrDefault(tag, 0) + 1;
        tagCt.put(tag, count);
        totalTagCt++;
    }

    public Integer getTagwrdCount(String tag, String wrd) {
        return tagwrdCount.getOrDefault(tag, new HashMap<>()).getOrDefault(wrd, 0);
    }

    public Integer getwrdtagCt(String tag, String wrd) {
        return wrdtagCt.getOrDefault(wrd, new HashMap<>()).getOrDefault(tag, 0);
    }

    public void incrementTagwrdCount(String tag, String wrd) {
        if (!tagwrdCount.containsKey(tag)) {
            tagwrdCount.put(tag, new HashMap<>());
        }

        Integer tWCount = tagwrdCount.get(tag).getOrDefault(wrd, 0) + 1;
        tagwrdCount.get(tag).put(wrd, tWCount);

        if (!wrdtagCt.containsKey(wrd)) {
            wrdtagCt.put(wrd, new HashMap<>());
        }

        Integer wTCount = wrdtagCt.get(wrd).getOrDefault(tag, 0) + 1;
        wrdtagCt.get(wrd).put(tag, wTCount);
    }

    public Integer gettagTransCt(String fromTag, String toTag) {
    	
        return tagTransCt.getOrDefault(fromTag, new HashMap<>()).getOrDefault(toTag, 0);
    
    }

    public void incrementTagTansitionCount(String fromTag, String toTag) {
    	
        if (!tagTransCt.containsKey(fromTag)) {
            tagTransCt.put(fromTag, new HashMap<>());
        }

        Integer count = tagTransCt.get(fromTag).getOrDefault(toTag, 0) + 1;
        tagTransCt.get(fromTag).put(toTag, count);
    }

    public Double getEmissionprob(String tag, String wrd) {
        Integer tagAndwrdCount = tagwrdCount.get(tag).getOrDefault(wrd, 0);
        Integer tagOccurences = tagCt.get(tag);

        return tagAndwrdCount / (double) tagOccurences;
    }

    public Double getTransitionprob(String fromTag, String toTag) {
        Integer tagTotagCt = tagTransCt.get(fromTag).getOrDefault(toTag, 0);
        Integer tagOccurences = tagCt.get(fromTag);

        return tagTotagCt / (double) tagOccurences;
    }

    public Double getStartprob(String tag) {
        Integer startCount = tagStartCount.getOrDefault(tag, 0);

        return startCount / (double) senCt;
    }

}