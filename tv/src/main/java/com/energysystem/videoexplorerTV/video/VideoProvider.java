/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.energysystem.videoexplorerTV.video;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.energysystem.videoexplorerTV.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * This class loads videos from a backend and saves them into a HashMap
 */
public class VideoProvider {

    private static final String TAG = "VideoProvider";
    private static String TAG_MEDIA = "videos";
    private static String TAG_GOOGLE_VIDEOS = "googlevideos";
    private static String TAG_CATEGORY = "category";
    private static String TAG_STUDIO = "studio";
    private static String TAG_SOURCES = "sources";
    private static String TAG_DESCRIPTION = "description";
    private static String TAG_CARD_THUMB = "card";
    private static String TAG_BACKGROUND = "background";
    private static String TAG_TITLE = "title";

    private static HashMap<String, List<Video>> mMovieList;
    private static Context mContext;
    private static String mPrefixUrl;

    public static void setContext(Context context) {
        if (mContext == null)
            mContext = context;
    }

    protected JSONObject parseUrl(String urlString) {
        Log.d(TAG, "Parse URL: " + urlString);
        InputStream is = null;

        mPrefixUrl = mContext.getResources().getString(R.string.prefix_url);

        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            return new JSONObject(json);
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.d(TAG, "JSON feed closed", e);
                }
            }
        }
    }

    public static HashMap<String, List<Video>> getMovieList() {
        return mMovieList;
    }

    public static HashMap<String, List<Video>> buildMedia(String url, Cursor data)
            throws JSONException {

        mMovieList = new HashMap<String, List<Video>>();
        mMovieList.clear();
        Log.e("Abrimos","Videos");
        List<Video> categoryList = new ArrayList<Video>();
        List<Video> categoryList2 = new ArrayList<Video>();

                while (data.moveToNext()) {
                        int videoID = data.getInt(data.getColumnIndex(MediaStore.Video.Media._ID));
                        int title = data.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                        int category = data.getColumnIndexOrThrow(MediaStore.Video.Media.CATEGORY);
                        int durationID = data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                        int data_string = data.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                        //int videoUrl = data.getColumnIndexOrThrow(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString());
                       // int bgImageUrl = data.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI.toString());

                    int duration = Integer.parseInt(data.getString(durationID));

                    String categoria = new File(data.getString(data_string)).getParent();
                    Log.e("Ruta:", categoria);
                    if(categoria.equals("/storage/emulated/0/Movies")) {
                        categoryList.add(buildMovieInfo(categoria, data.getString(title), "Descripcion", String.format("%d min, %d sec",
                                        TimeUnit.MILLISECONDS.toMinutes(duration),
                                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                                ),
                                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID).toString(), ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID).toString(),
                                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID).toString()));
                        mMovieList.put("Almacenamiento Interno", categoryList);
                    }
                    else
                    {
                        categoryList2.add(buildMovieInfo(categoria, data.getString(title), "Descripcion", String.format("%d min, %d sec",
                                        TimeUnit.MILLISECONDS.toMinutes(duration),
                                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                                ),
                                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID).toString(), ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID).toString(),
                                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoID).toString()));
                        mMovieList.put("USB", categoryList2);
                    }
                }



        /*JSONObject jsonObj = new VideoProvider().parseUrl(url);
        JSONArray categories = jsonObj.getJSONArray(TAG_GOOGLE_VIDEOS);
        if (null != categories) {
            Log.d(TAG, "category #: " + categories.length());
            String title;
            String videoUrl;
            String bgImageUrl;
            String cardImageUrl;
            String studio;
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                String category_name = category.getString(TAG_CATEGORY);
                JSONArray videos = category.getJSONArray(TAG_MEDIA);
                Log.d(TAG,
                        "category: " + i + " Name:" + category_name + " video length: "
                                + videos.length());
                List<Video> categoryList = new ArrayList<Video>();
                if (null != videos) {
                    for (int j = 0; j < videos.length(); j++) {
                        JSONObject video = videos.getJSONObject(j);
                        String description = video.getString(TAG_DESCRIPTION);
                        JSONArray videoUrls = video.getJSONArray(TAG_SOURCES);
                        if (null == videoUrls || videoUrls.length() == 0) {
                            continue;
                        }
                        title = video.getString(TAG_TITLE);
                        videoUrl = getVideoPrefix(category_name, videoUrls.getString(0));
                        bgImageUrl = getThumbPrefix(category_name, title,
                                video.getString(TAG_BACKGROUND));
                        cardImageUrl = getThumbPrefix(category_name, title,
                                video.getString(TAG_CARD_THUMB));
                        studio = video.getString(TAG_STUDIO);
                        categoryList.add(buildMovieInfo(category_name, title, description, studio,
                                videoUrl, cardImageUrl,
                                bgImageUrl));
                    }
                    mMovieList.put(category_name, categoryList);
                }
            }
        }*/
        Log.e("VideoProvider","Tamano mMovie: "+mMovieList.size());
        return mMovieList;
    }

    private static Video buildMovieInfo(String category, String title,
            String description, String studio, String videoUrl, String cardImageUrl,
            String bgImageUrl) {
        Video video = new Video();
        video.setId(Video.getCount());
        Video.incCount();
        video.setTitle(title);
        video.setDescription(description);
        video.setStudio(studio);
        video.setCategory(category);
        video.setCardImageUrl(cardImageUrl);
        video.setBackgroundImageUrl(bgImageUrl);
        video.setVideoUrl(videoUrl);

        return video;
    }

    private static String getVideoPrefix(String category, String videoUrl) {
        String ret = "";
        ret = mPrefixUrl + category.replace(" ", "%20") + '/' +
                videoUrl.replace(" ", "%20");
        return ret;
    }

    private static String getThumbPrefix(String category, String title, String imageUrl) {
        String ret = "";

        ret = mPrefixUrl + category.replace(" ", "%20") + '/' +
                title.replace(" ", "%20") + '/' +
                imageUrl.replace(" ", "%20");
        return ret;
    }
}
