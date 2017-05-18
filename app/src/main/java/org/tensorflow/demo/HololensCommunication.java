package org.tensorflow.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class HololensCommunication {

    private Activity activity;
    AsyncHttpClient client = new AsyncHttpClient();
    String baseAddress;
    String serializedImage = "";
    ImageView imageView;
boolean newImage =false;

    Bitmap deserializedImage;

    public Bitmap getDeserializedImage() {
        return deserializedImage;
    }

    public boolean hasNewImage() {
        return newImage;
    }

    public void setNewImage(boolean newImage) {
        this.newImage = newImage;
    }

    public void setDeserializedImage(Bitmap bitmap) {
        deserializedImage=bitmap;
    }

    public HololensCommunication(Activity activity, String baseAddress, ImageView imageView) {
        this.activity = activity;
        this.baseAddress = baseAddress;
        this.imageView = imageView;
        Log.d("message", "Hololenscom initialized");
    }

    public void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }

    void sendToHoloLensRoute(String route, String message) {
        sendToHoloLens(baseAddress + route, message);
    }

    void sendToHoloLensPost(String url, String message) {
        Log.d("message", url);
        //  RequestParams params = new RequestParams("body", new JSONObject());
        StringEntity entity = null;

        try {
            entity = new StringEntity(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        client.post(activity.getApplicationContext(), url, entity, "application/json", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.d("message", "onFailure POST");
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                Log.d("message", "onSuccess POST");
            }
        });
    }


    void sendToHoloLens(String url, String message) {
        Log.d("message", url);
        //  RequestParams params = new RequestParams("body", new JSONObject());
        StringEntity entity = null;

        try {
            entity = new StringEntity(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        client.get(activity.getApplicationContext(), url, entity, "application/json", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("message", "onFailure");

            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d("message", "onSuccess");
                Log.d("message", "onSuccess body: " + responseString);
                Log.d("message", "onSuccess body length: " + responseString.length());
                int t = 0;
                int j = 100;
                /*
                while (j < responseString.length()) {

                    Log.d("message " + t, "" + responseString.substring(t, j));
                    j += 100;
                    t += 100;
                }

                Log.d("message " + t, "" + responseString.substring(t, responseString.length()));
*/

                byte[] decodedImage = Base64.decode(responseString, Base64.DEFAULT);


                String imageAsString = "";
/*
                for (int i = 0; i < decodedImage.length; i++) {
                    imageAsString += decodedImage[i] + " ";
                }
                Log.d("message", "decoded int byte arrayAsString: " + imageAsString);
*/
                Log.d("message", "decoded int byte array: " + decodedImage);
                deserializedImage = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
                Log.d("message", "The deserializedImage: " + deserializedImage);
                setNewImage(true);
/*
                imageView.setImageBitmap(Bitmap.createScaledBitmap(deserializedImage, imageView.getWidth(),
                        imageView.getHeight(), false));
                */
            }
        });
    }
}
