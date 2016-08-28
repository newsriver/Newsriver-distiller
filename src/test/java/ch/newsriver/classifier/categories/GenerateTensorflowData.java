package ch.newsriver.classifier.categories;


import ch.newsriver.ml.classifier.news.category.ArticleTrainingSet;
import ch.newsriver.ml.classifier.news.category.TrainingDataHandler;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.min;

/**
 * Created by eliapalme on 29/07/16.
 */
public class GenerateTensorflowData {


    final boolean GENERATE_CROSS = false;
    final int SAMPLESIZE = 50000;
    final int TEXTLENGHT = 1000;
    List<String> allStopWords = null;
    private Random random = new Random();
    private AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;

    @Ignore  //Not a real test only used as utility class to train the classifier
    @Test
    public void trainModel() throws URISyntaxException, Exception {

        TrainingDataHandler handler = new TrainingDataHandler();


        //handler.downloadNewDataSet(50000, 3);

        final String location = "/Users/eliapalme/Newsriver/Newsriver-classifier/training/data/";

        Map<Integer, Map<Integer, ArticleTrainingSet>> trainingSet = handler.loadData(3);
        Map<Integer, ArticleTrainingSet> lang = trainingSet.get(3);

        PrintWriter writerRandom = new PrintWriter(location + "train/" + 999 + ".random.samples", "UTF-8");

        int randomSamplesNum = SAMPLESIZE / lang.values().size();


        int pos = 0;
        int neg = 0;
        for (ArticleTrainingSet category : lang.values()) {


            Collections.shuffle(category.getArticles());


            for (int i = 0; i < min(randomSamplesNum, category.getArticles().size()); i++) {
                writeArticle(category.getArticles().get(i), writerRandom);
            }

            if (category.getArticles().size() < SAMPLESIZE) {
                System.out.println("Ccategory:" + category.getCategory() + " has not enough samples");
                continue;
            }

            PrintWriter writerTrain = new PrintWriter(location + "train/" + category.getCategoryId() + "." + category.getCategory() + ".samples", "UTF-8");
            PrintWriter crossValidation = null;
            if (GENERATE_CROSS) {
                crossValidation = new PrintWriter(location + "cross/" + category.getCategoryId() + "." + category.getCategory() + ".samples", "UTF-8");
            }


            int count = 0;
            for (ArticleTrainingSet.Article article : category.getArticles()) {

                if (count >= SAMPLESIZE) {
                    break;
                }
                count++;

                PrintWriter writer;
                if (GENERATE_CROSS && random.nextBoolean()) {
                    writer = crossValidation;
                } else {
                    writer = writerTrain;
                }

                writeArticle(article, writer);

            }
            writerTrain.close();
            if (crossValidation != null) crossValidation.close();

        }


    }

    private void writeArticle(ArticleTrainingSet.Article article, PrintWriter writer) throws Exception {

        String text = (article.getTitle() + " " + article.getText()).replaceAll("[\n\r]", " ");

        text = text.substring(0, min(TEXTLENGHT * 3, text.length()));

        StandardTokenizer tokenizer = new StandardTokenizer(factory);
        tokenizer.setReader(new StringReader(text));
        TokenStream streamStop = new StopFilter(tokenizer, StopAnalyzer.ENGLISH_STOP_WORDS_SET);

        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        streamStop.reset();
        while (streamStop.incrementToken()) {
            String term = charTermAttribute.toString();
            sb.append(term + " ");
        }

        text = sb.toString().trim();
        text = text.substring(0, min(TEXTLENGHT, text.length()));
        text = text.substring(0, text.lastIndexOf(" "));
        text = text.trim();
        writer.println(text);

    }
}
