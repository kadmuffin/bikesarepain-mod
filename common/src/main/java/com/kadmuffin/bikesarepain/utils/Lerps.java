package com.kadmuffin.bikesarepain.utils;

public class Lerps {
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

}
