package com.modelcity.common.util;

import java.util.ArrayList;
import java.util.List;

/** Helpers for the common {@code photoUrl1..3} pattern across views and DTOs. */
public final class PhotoUrls {

    private PhotoUrls() {
    }

    /** Collects the non-null photo URLs into a list (preserves order, max 3 entries). */
    public static List<String> collect(String url1, String url2, String url3) {
        List<String> photos = new ArrayList<>(3);
        if (url1 != null) {
            photos.add(url1);
        }
        if (url2 != null) {
            photos.add(url2);
        }
        if (url3 != null) {
            photos.add(url3);
        }
        return photos;
    }
}
