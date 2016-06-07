package ch.newsriver.distiller.signals.sentiment;

import ch.newsriver.data.content.Article;
import ch.newsriver.data.metadata.FinancialSentiment;
import ch.newsriver.data.metadata.ReadTime;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashSet;

/**
 * Created by eliapalme on 07/06/16.
 */
public class FinancialSentimentScore {

    private static final Logger logger = LogManager.getLogger(FinancialSentimentScore.class);
    private final int DECIMAL_NUMBERS = 2;
    HashSet<String> postiveWords = new HashSet<>();
    HashSet<String> negativeWords = new HashSet<>();

    public FinancialSentimentScore() {


        try (InputStream in = getClass().getResourceAsStream("negative.txt"); BufferedReader input = new BufferedReader(new InputStreamReader(in));) {
            input.lines().forEach(word -> negativeWords.add(word));
        } catch (IOException e) {
            logger.fatal("Unable to load negative words", e);
        }

        try (InputStream in = getClass().getResourceAsStream("positive.txt"); BufferedReader input = new BufferedReader(new InputStreamReader(in));) {
            input.lines().forEach(word -> postiveWords.add(word.trim()));
        } catch (IOException e) {
            logger.fatal("Unable to load negative words", e);
        }


    }


    /*
        Based on Bill McDonald and Tim Loughran words list: http://www3.nd.edu/~mcdonald/Word_Lists.html
    */
    public Article process(Article article) {

        //currently we only support english
        if (!article.getLanguage().equalsIgnoreCase("en")) return article;
        float positiveCount = 0;
        float negativeCount = 0;
        float wordsCount = 0;
        String[] words = (article.getTitle() + article.getText()).split("\\s+");
        for (String word : words) {
            word = word.replaceAll("[^\\w]", "");
            if (word.length() < 3) continue;
            wordsCount++;
            if (postiveWords.contains(word.toUpperCase())) {
                positiveCount++;
            }
            if (negativeWords.contains(word.toUpperCase())) {
                negativeCount++;
            }
        }
        float score;
        if (positiveCount > 0 || negativeCount > 0) {
            if (positiveCount > negativeCount) {
                score = positiveCount / wordsCount;
            } else {
                score = negativeCount / wordsCount * -1;
            }
            //only consider significant scores.
            score = roundDecimal(score);
            if (score > 0.01 || score < -0.01) {
                FinancialSentiment finSentiment = new FinancialSentiment();
                finSentiment.setSentiment(score);
                article.addMetadata(finSentiment);
            }
        }

        return article;

    }

    private float roundDecimal(float number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(DECIMAL_NUMBERS, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
