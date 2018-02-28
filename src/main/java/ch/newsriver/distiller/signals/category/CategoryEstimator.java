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

    public static Category estimateCategory(Article article) {

        HashMap<String, Integer> categories = new HashMap<>();
        HashMap<String, Integer> regions = new HashMap<>();
        HashMap<String, Integer> countries = new HashMap<>();
        HashMap<String, Integer> countryCodes = new HashMap<>();


        for (BaseURL referral : article.getReferrals()) {
            if (referral instanceof FeedURL) {
                String category = ((FeedURL) referral).getCategory();
                String region = ((FeedURL) referral).getRegion();
                String country = ((FeedURL) referral).getCountry();
                String countryCode = ((FeedURL) referral).getCountryCode();

                if (category != null) {
                    incOccurrence(categories, category);
                }
                if (region != null) {
                    incOccurrence(regions, region);
                }
                if (country != null) {
                    incOccurrence(countries, country);
                }
                if (countryCode != null) {
                    incOccurrence(countryCodes, countryCode);
                }
            }
            if (referral instanceof LinkURL) {
                String category = ((LinkURL) referral).getCategory();
                String region = ((LinkURL) referral).getRegion();
                String country = ((LinkURL) referral).getCountry();
                String countryCode = ((LinkURL) referral).getCountryCode();

                if (category != null) {
                    incOccurrence(categories, category);
                }
                if (region != null) {
                    incOccurrence(regions, region);
                }
                if (country != null) {
                    incOccurrence(countries, country);
                }
                if (country != null) {
                    incOccurrence(countries, country);
                }
                if (countryCode != null) {
                    incOccurrence(countryCodes, countryCode);
                }
            }
        }


        Category category = new Category();
        category.setCategory(getMaxOccurrence(categories));
        category.setRegion(getMaxOccurrence(regions));
        category.setCountry(getMaxOccurrence(countries));
        category.setCountryCode(getMaxOccurrence(countryCodes));


        return category;

    }

    private static String getMaxOccurrence(HashMap<String, Integer> map) {
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

    private static void incOccurrence(HashMap<String, Integer> map, String key) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

    @Override
    public Article process(Article article) {


        Category category = CategoryEstimator.estimateCategory(article);

        if (category.getCategory() != null) {
            article.addMetadata(category);
        }

        return article;
    }

}
