package com.kadmuffin.bikesarepain.client.helper;

import com.kadmuffin.bikesarepain.client.item.BaseItemRenderer;
import com.mojang.datafixers.types.Func;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Utils<T> {
    public static <T> Map<String, Function<T, Integer>> createBonesToColorMap(Map<List<String>, Function<T, Integer>> bonesToColor) {
        Map<String, Function<T, Integer>> map = new HashMap<>();
        bonesToColor.forEach((bones, color) -> {
            bones.forEach(bone -> map.put(bone, color));
        });
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

    public static <T> Map<String, Integer> evaluateBonesToColorMap(Map<String, Function<T, Integer>> bonesToColor, T stack) {
        Map<String, Integer> map = new HashMap<>();
        bonesToColor.forEach((bone, color) -> {
            map.put(bone, color.apply(stack));
        });
        return map;
    }
}
