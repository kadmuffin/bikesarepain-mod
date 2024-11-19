package com.kadmuffin.bikesarepain.utils;

import java.util.ArrayList;
import java.util.List;

public class ColorUtil {
    public static int toRGB(int color) {
        // Mask out the RGB components (clear the alpha channel)
        int rgb = color & 0x00FFFFFF;

        // Add the alpha channel (set to 255, or 0xFF)
        return 0xFF000000 | rgb;
    }

    public static List<Integer> toRGB(List<Integer> colors) {
        ArrayList<Integer> noAlphaColors = new ArrayList<>();

        colors.forEach(color -> noAlphaColors.add(toRGB(color)));

        return noAlphaColors;
    }
}
