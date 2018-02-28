package ch.newsriver.distiller.signals.category;

import ch.newsriver.data.content.Article;
import ch.newsriver.data.metadata.Category;
import ch.newsriver.distiller.signals.SignalEstimator;
import ch.newsriver.distiller.signals.sentiment.FinancialSentimentScore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import java.util.List;

/**
 * Created by eliapalme on 06/06/16.
 */
public class CategoryClassifier implements SignalEstimator {


    private static final Logger logger = LogManager.getLogger(FinancialSentimentScore.class);

    private SavedModelBundle bundle;

    public CategoryClassifier(String modelPath) {
        bundle = SavedModelBundle.load(modelPath, "serve");

    }


    @Override
    public Article process(Article article) {

        //Currently the classifier only supports Italian
        if (!article.getLanguage().equalsIgnoreCase("it"))
            return article;

        //Only classify articles that have an unknown category
        if (article.getMetadata().containsKey("category"))
            return article;


        Session session = bundle.session();
        try {
            String words[] = (article.getTitle() + " " + article.getText()).split(" ");

            byte[][][] stringMatrix = new byte[1][words.length][];
            for (int i = 0; i < words.length; ++i) {
                stringMatrix[0][i] = String.format(words[i]).getBytes("UTF-8");
            }

            byte[][] urlMatrix = new byte[1][];
            urlMatrix[0] = String.format(article.getUrl()).getBytes("UTF-8");


            Tensor<String> text = Tensors.create(stringMatrix);
            Tensor<String> url = Tensors.create(urlMatrix);

            //Input and output names have been retrived from model with command
            //saved_model_cli show  --dir ./outputdir/export/Servo/1517653989/ --tag_set serve --signature_def default_input_alternative:default_output_alternative
            final String inputName_text = "Placeholder:0";
            final String inputName_url = "Placeholder_1:0";
            final String probsName = "Softmax:0";
            final String probIndexName = "ArgMax_1:0";
            final String className = "Gather:0";


            List<Tensor<?>> tensors =
                    session
                            .runner()
                            .feed(inputName_text, text)
                            .feed(inputName_url, url)
                            .fetch(probsName)
                            .fetch(className)
                            .fetch(probIndexName)
                            .run();

            String categoryName = new String(tensors.get(1).copyTo(new byte[1][])[0], "utf-8");

            Category category = CategoryEstimator.estimateCategory(article);
            category.setCategory(categoryName);
            article.addMetadata(category);

        } catch (Exception e) {
            logger.error("Error with TF category classifier.", e);
        }

        return article;

    }

}
