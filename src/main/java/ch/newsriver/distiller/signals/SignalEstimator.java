package ch.newsriver.distiller.signals;

import ch.newsriver.data.content.Article;

/**
 * Created by eliapalme on 28/08/16.
 */
public interface SignalEstimator {

    public Article process(Article article);
}
