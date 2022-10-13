package ru.yandex.practicum.filmorate.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollaborativeFiltering {

    //Simple item-based Collaborative Filtering algorithm
    //baseId - target user id
    //data - HashMap with information about user item likes (e.g. film likes)
    public static List<Integer> recommendItems(Map<Integer, List<Integer>> data, Integer baseId) {
        List<Integer> baseItems = data.get(baseId); //target user id likes information
        Integer similarId = null;
        int score = 0;

        for (Map.Entry<Integer, List<Integer>> e : data.entrySet()) {
            if(!e.getKey().equals(baseId)) {
                int k = 0;
                for(Integer item : e.getValue()) {
                    if(baseItems.contains(item)) {
                        k++;
                    }
                    if (k > score) {
                        score = k;
                        similarId = e.getKey();
                    }
                }
            }
        }

        if (similarId == null) {    // no users with similar likes
            return Collections.emptyList();
        }

        List<Integer> itemsToRecommend = data.get(similarId);
        return itemsToRecommend.stream()
                .filter(x -> !baseItems.contains(x)) //exclude items that target user
                .collect(Collectors.toList());     //already liked
    }
}
