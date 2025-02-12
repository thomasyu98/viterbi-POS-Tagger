package viterbi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WSJPOSTagger {

    private static Integer MAX_SUFFIX_LENGTH;
    private static Integer MAX_WORD_FREQUENCY;

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            String errMsg = "Expected [TRAINING_FILENAME] [TEST_WORDS_FILENAME] [MAX_SUFFIX_LENGTH] [MAX_WORD_FREQUENCY], got " + args.length + " args.";
            System.err.println(errMsg);
            System.exit(99);
        }

        System.out.println("Training model..");
        MAX_SUFFIX_LENGTH = Integer.parseInt(args[2]);
        MAX_WORD_FREQUENCY = Integer.parseInt(args[3]);
        System.out.println("Max suffix length: " + MAX_SUFFIX_LENGTH);
        System.out.println("Max frequency: " + MAX_WORD_FREQUENCY + " to create suffix tree");

        String trainFilename = args[0];
        File trainFile = new File(trainFilename);
        BigramModel bigramModel = new BigramModel(MAX_SUFFIX_LENGTH);
        bigramModel.train(trainFile);

        SuffixTreeBuilder treeBuilder = new SuffixTreeBuilder(bigramModel, MAX_SUFFIX_LENGTH, MAX_WORD_FREQUENCY);
        SuffixTree upperCaseTree = treeBuilder.buildUpperCaseTree();
        SuffixTree lowerCaseTree = treeBuilder.buildLowerCaseTree();

        String testFilename = args[1];
        File testFile = new File(testFilename);
        String[] filenameParts = testFilename.split("/");
        String[] filenameAndExt = filenameParts[filenameParts.length - 1].split("\\.");
        String filename = filenameAndExt[0];
        String outputFilename =  "submission.pos";

        System.out.println("Training done\nNow time to eval...");

        EvaluationResult result = bigramModel.evaluate(upperCaseTree, lowerCaseTree, testFile, outputFilename);
        generateOutputFile(outputFilename, result);
        System.out.println("Output file can be found in root directory");
    }

    public static void generateOutputFile(String filename, EvaluationResult result) throws IOException {
        FileWriter writer = new FileWriter(filename);
        List<List<String>> sentences = result.sentences;
        List<List<String>> sentenceTags = result.sentenceTags;

        Integer numSentences = sentences.size();
        for (int i = 0; i < numSentences; i++) {
            Integer sentenceLength = sentences.get(i).size();
            for (int j = 0; j < sentenceLength; j++) {
                writer.write(sentences.get(i).get(j) + "\t" + sentenceTags.get(i).get(j) + "\n");
            }
            writer.write("\n");
        }

        writer.close();
    }

}