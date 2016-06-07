package ch.newsriver.distiller;

import ch.newsriver.distiller.signals.ReadTimeEstimator;
import ch.newsriver.data.content.Article;
import ch.newsriver.data.content.ArticleFactory;
import ch.newsriver.distiller.signals.sentiment.FinancialSentimentScore;
import ch.newsriver.executable.Main;
import ch.newsriver.executable.poolExecution.MainWithPoolExecutorOptions;
import ch.newsriver.performance.MetricsLogger;
import ch.newsriver.stream.Stream;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.time.Duration;

/**
 * Created by eliapalme on 06/06/16.
 */
public class DistillerMain extends MainWithPoolExecutorOptions {


    private static final int DEFAUTL_PORT = 9093;
    private static final Logger logger = LogManager.getLogger(DistillerMain.class);
    private static MetricsLogger metrics;
    private static int MAX_EXECUTUION_DURATION = 120;
    public int getDefaultPort(){
        return DEFAUTL_PORT;
    }

    Stream<Article,Article> stream;
    ReadTimeEstimator readTimeEstimator;
    FinancialSentimentScore finScore;

    public DistillerMain(String[] args){
        super(args,true);
        metrics = MetricsLogger.getLogger(DistillerMain.class, Main.getInstance().getInstanceName());
        readTimeEstimator = new ReadTimeEstimator();
        finScore = new FinancialSentimentScore();
    }

    public static void main(String[] args){
        new DistillerMain(args);

    }

    public void shutdown(){

        if(stream!=null)stream.shutdown();

    }

    public void start(){

        System.out.println("Threads pool size:" + this.getPoolSize() +"\tbatch size:"+this.getBatchSize()+"\tqueue size:"+this.getQueueSize());





        stream = Stream.Builder.with("Miner",this.getBatchSize(),this.getPoolSize(),this.getQueueSize(), Duration.ofSeconds(MAX_EXECUTUION_DURATION))
                .from("raw-article")
                .withClasses(Article.class,Article.class)
                .setProcessor(input -> {
                    Article article = (Article)input;
                    try {
                        article = readTimeEstimator.process(article);
                    }catch (Exception e){}

                    try {
                        article = finScore.process(article);
                    }catch (Exception e){}
                    //Update article reading time
                    ArticleFactory.getInstance().updateArticle(article);

                    return article;
                })
                .to("processed-article", v -> true).build();

        new Thread(stream).start();


    }


}
