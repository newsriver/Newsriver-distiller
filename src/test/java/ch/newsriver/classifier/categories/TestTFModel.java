package ch.newsriver.classifier.categories;

import org.junit.Ignore;
import org.junit.Test;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import java.util.Arrays;
import java.util.List;

/**
 * Created by eliapalme on 10.02.18.
 */
public class TestTFModel {


    private static final String SAVED_MODEL_PATH = "/Users/eliapalme/Newsriver/Newsriver-classifier/category-classifier/outputdir/export/Servo/1517653989/";

    @Ignore
    @Test
    public void test() throws Exception {


        try (SavedModelBundle bundle = SavedModelBundle.load(SAVED_MODEL_PATH, "serve")) {

            Session session = bundle.session();

            String words[] = "Roger Federer vince la coppa Devis ".split(" ");


            byte[][][] stringMatrix = new byte[1][words.length][];
            for (int i = 0; i < words.length; ++i) {
                stringMatrix[0][i] = String.format(words[i]).getBytes("UTF-8");
            }

            Tensor<String> t = Tensors.create(stringMatrix);

            //Input and output names have been retrived from model with command
            //saved_model_cli show  --dir ./outputdir/export/Servo/1517653989/ --tag_set serve --signature_def default_input_alternative:default_output_alternative
            final String inputName = "Placeholder:0";
            final String probsName = "Softmax:0";
            final String className = "Gather:0";


            List<Tensor<?>> tensors =
                    session
                            .runner()
                            .feed(inputName, t)
                            .fetch(probsName)
                            .fetch(className)
                            .run();

            String categoryName = new String(tensors.get(1).copyTo(new byte[1][])[0], "utf-8");

            System.out.println(Arrays.deepToString(tensors.get(0).copyTo(new float[1][14])));


        }
    }
}