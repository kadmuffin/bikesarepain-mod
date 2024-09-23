package com.kadmuffin.bikesarepain.client.helper;

import java.util.*;
import java.util.function.Function;

public class Utils {
    public static <T> Map<String, Function<T, Integer>> createBonesToColorMap(Map<List<String>, Function<T, Integer>> bonesToColor) {
        Map<String, Function<T, Integer>> map = new HashMap<>();
        bonesToColor.forEach((bones, color) -> bones.forEach(bone -> map.put(bone, color)));
        return map;
    }

    public static Map<String, Integer> createZeroedColorMap(List<String> bones) {
        Map<String, Integer> map = new HashMap<>();
        bones.forEach(bone -> map.put(bone, 0));
        return map;
    }

    public static <T> List<T> completeRest(List<T> baseInm, List<T> target) {
        List<T> base = new ArrayList<>(baseInm);

        if (base.size() < target.size()) {
            for (int i = base.size(); i < target.size(); i++) {
                base.add(target.get(i));
            }
        } else if (base.size() > target.size()) {
            base = new ArrayList<>(base.subList(0, target.size()));
        }

        return base;
    }

    // Same as completeRest, but when at an index of the baseInm there is a zero, it gets replaced by the target.value at that index
    public static ArrayList<Integer> completeRestIncludingZeroes(List<Integer> baseInm, List<Integer> target) {
        ArrayList<Integer> base = new ArrayList<>(baseInm);

        if (base.size() < target.size()) {
            for (int i = base.size(); i < target.size(); i++) {
                base.add(target.get(i));
            }
        } else if (base.size() > target.size()) {
            for (int i = 0; i < target.size(); i++) {
                if (base.get(i) == 0) {
                    base.set(i, target.get(i));
                }
            }
        }

        return base;
    }

    public static <T> Map<String, Integer> evaluateBonesToColorMap(Map<String, Function<T, Integer>> bonesToColor, T stack) {
        Map<String, Integer> map = new HashMap<>();
        bonesToColor.forEach((bone, color) -> map.put(bone, color.apply(stack)));
        return map;
    }

    public static float wrapRotation(float angle) {
        return (float) (((angle % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI));
    }
}
