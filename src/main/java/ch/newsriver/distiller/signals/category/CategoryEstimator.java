package ch.newsriver.distiller.signals.category;

import ch.newsriver.data.content.Article;
import ch.newsriver.data.metadata.Category;
import ch.newsriver.data.url.BaseURL;
import ch.newsriver.data.url.FeedURL;
import ch.newsriver.data.url.LinkURL;
import ch.newsriver.distiller.signals.SignalEstimator;

import java.util.HashMap;

/**
 * Created by eliapalme on 28/08/16.
 */
public class CategoryEstimator implements SignalEstimator {


    //Process all existing referrals and compute the most frequently defined category, region and country.

    @Override
    public Article process(Article article) {


        HashMap<String, Integer> categories = new HashMap<>();
        HashMap<String, Integer> regions = new HashMap<>();
        HashMap<String, Integer> countries = new HashMap<>();
        for (BaseURL referal : article.getReferrals()) {
            if (referal instanceof FeedURL) {
                String category = ((FeedURL) referal).getCategory();
                String region = ((FeedURL) referal).getRegion();
                String country = ((FeedURL) referal).getCountry();
                if (category != null) {
                    incOccurrence(categories, category);
                }
                if (region != null) {
                    incOccurrence(regions, region);
                }
                if (country != null) {
                    incOccurrence(countries, country);
                }
            }
            if (referal instanceof LinkURL) {
                String category = ((LinkURL) referal).getCategory();
                String region = ((LinkURL) referal).getRegion();
                String country = ((LinkURL) referal).getCountry();
                if (category != null) {
                    incOccurrence(categories, category);
                }
                if (region != null) {
                    incOccurrence(regions, region);
                }
                if (country != null) {
                    incOccurrence(countries, country);
                }
            }
        }


        if (!categories.isEmpty()) {
            Category category = new Category();
            category.setCategory(getMaxOccurrence(categories));
            category.setRegion(getMaxOccurrence(regions));
            category.setCountry(getMaxOccurrence(countries));

            article.addMetadata(category);
        }


        return article;
    }

    private String getMaxOccurrence(HashMap<String, Integer> map) {
        String maxKey = null;
        int max = 0;
        for (String key : map.keySet()) {
            if (max < map.get(key)) {
                max = map.get(key);
                maxKey = key;
            }
        }
        return maxKey;
    }

    private void incOccurrence(HashMap<String, Integer> map, String key) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

}
