package ch.newsriver.distiller.signals;

import ch.newsriver.data.content.Article;
import ch.newsriver.data.content.Element;
import ch.newsriver.data.content.ImageElement;
import ch.newsriver.data.metadata.ReadTime;

/**
 * Created by eliapalme on 07/06/16.
 */
public class ReadTimeEstimator {


    /*
        Based on Medium post: https://medium.com/the-story/read-time-and-you-bc2048ab620c#.gyley8oqj
    */
    public Article process(Article article) {


        float readSeconds = 0;
        float wordCount = (article.getTitle() + article.getText()).split("\\s+").length;

        //275 word per minute or 4.58333 word per second
        readSeconds += wordCount / 4.583333;
        int imageCount = 0;
        for (Element element : article.getElements()) {
            if (element instanceof ImageElement) {
                if (imageCount > 10) {
                    readSeconds += 3;
                } else {
                    readSeconds += (12 - imageCount);
                }
                imageCount++;
            }
        }
        if (readSeconds > 0) {
            ReadTime readTime = new ReadTime();
            readTime.setSeconds(Math.round(readSeconds));
            article.addMetadata(readTime);
        }

        return article;

    }

}
