/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.demo;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Trace;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.OnyxBeaconManager;
import com.onyxbeacon.listeners.OnyxBeaconsListener;
import com.onyxbeacon.rest.auth.util.AuthenticationMode;
import com.onyxbeaconservice.IBeacon;

import java.util.List;
import java.util.Vector;
import java.util.jar.Manifest;

import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.beacon.BleStateListener;
import org.tensorflow.demo.beacon.BleStateReceiver;
import org.tensorflow.demo.beacon.ContentReceiver;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClassifierActivity extends CameraActivity implements BleStateListener,OnImageAvailableListener ,OnyxBeaconsListener{
    private static final Logger LOGGER = new Logger();
    private OnyxBeaconManager mManager;
    private ContentReceiver contentReceiver;
    private BleStateReceiver mBleReceiver;

    // These are the settings for the original v1 Inception model. If you want to
    // use a model that's been produced from the TensorFlow for Poets codelab,
    // you'll need to set IMAGE_SIZE = 299, IMAGE_MEAN = 128, IMAGE_STD = 128,
    // INPUT_NAME = "Mul:0", and OUTPUT_NAME = "final_result:0".
    // You'll also need to update the MODEL_FILE and LABEL_FILE paths to point to
    // the ones you produced.
    //
    // To use v3 Inception model, strip the DecodeJpeg Op from your retrained
    // model first:
    //
    // python strip_unused.py \
    // --input_graph=<retrained-pb-file> \
    // --output_graph=<your-stripped-pb-file> \
    // --input_node_names="Mul" \
    // --output_node_names="final_result" \
    // --input_binary=true

    ///original values///
/*
  private static final int INPUT_SIZE = 224;
  private static final int IMAGE_MEAN = 117;
  private static final float IMAGE_STD = 1;
  private static final String INPUT_NAME = "input";
  private static final String OUTPUT_NAME = "output";

  private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
  private static final String LABEL_FILE =
      "file:///android_asset/imagenet_comp_graph_label_strings.txt";
*/

    ///new values///
    ////// UNCOMMENT AFTER TECHSYLVANIA

    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";



/*
    private static final String MODEL_FILE = "file:///android_asset/retrained_graph_optimized.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/retrained_labels.txt";
*/

    ///////FOR TECHSYLVANIA


    ////////////////////

    ////new values end///


    ///with background
//UNCOMMENT AFTER TECHSYLVANIA
 //   private static final String MODEL_FILE = "file:///android_asset/retrained_graph_background_optimized.pb";
 //   private static final String LABEL_FILE =
 //           "file:///android_asset/retrained_labels_background.txt";
    ///with background end

//TECHSYLVANIA
    private static final String MODEL_FILE = "file:///android_asset/retrained_graph_blind_optimized.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/retrained_labels_blind.txt";
//TECHSYLVANIA END


    private static final boolean SAVE_PREVIEW_BITMAP = false;

    private static final boolean MAINTAIN_ASPECT = true;

    private Classifier classifier;

    private Integer sensorOrientation;

    private int previewWidth = 0;
    private int previewHeight = 0;
    private byte[][] yuvBytes;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private Bitmap cropCopyBitmap;

    private boolean computing = false;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private ResultsView resultsView;

    private BorderedText borderedText;

    private long lastProcessingTimeMs;

    private String textToSend= "";

    private HololensCommunication hololensCommunication;
    private int interval = 1000; // 5 seconds by default, can be changed later
    private Handler handler;

    Bitmap bm;
    String baseAddress = "http://192.168.43.166" + ":" +
            "5555" + "/";
    boolean sendingResultsToHololens = false;

    @BindView(R.id.picturetorecognize)
    ImageView pictureToRecognize;

    @OnClick(R.id.getimage)
    public void submit(View view) {
        Log.d("message", "in onClick buttonsend");

        requestImageFromHololens();
    }

    private void requestImageFromHololens() {
        hololensCommunication.setBaseAddress(baseAddress);
        String url = baseAddress +
                "toggleVisibility";
        String message = "{}";
        hololensCommunication.sendToHoloLens(url, message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        hololensCommunication = new HololensCommunication(this, "http://192.168.43.166:5555/", null);
        handler = new Handler();
        startRepeatingTask();
        // submit(null);


        Context context = this.getApplicationContext();
        mManager = OnyxBeaconApplication.getOnyxBeaconManager(context);
        contentReceiver=new ContentReceiver(this);
        contentReceiver.setOnyxBeaconsListener(this);

        mBleReceiver = BleStateReceiver.getInstance();
        mBleReceiver.setBleStateListener(this);
        if (mManager.isBluetoothAvailable()) {
            mManager.initSDK(AuthenticationMode.CLIENT_SECRET_BASED);
            mManager.setCouponEnabled(true);
            mManager.setAPIContentEnabled(true);
        } else {
            mManager.enableBluetooth();
        }
    }



    @Override
    protected int getLayoutId() {

        return R.layout.camera_connection_fragment;

    }

    @Override
    protected int getDesiredPreviewFrameSize() {
        return INPUT_SIZE;
    }

    private static final float TEXT_SIZE_DIP = 10;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        classifier =
                TensorFlowImageClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);

        resultsView = (ResultsView) findViewById(R.id.results);
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);

        sensorOrientation = rotation + screenOrientation;

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbBytes = new int[previewWidth * previewHeight];
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        INPUT_SIZE, INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        yuvBytes = new byte[3][];

        addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        renderDebug(canvas);
                    }
                });
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;

        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (computing) {
                image.close();
                return;
            }
            computing = true;

            Trace.beginSection("imageAvailable");

            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            ImageUtils.convertYUV420ToARGB8888(
                    yuvBytes[0],
                    yuvBytes[1],
                    yuvBytes[2],
                    rgbBytes,
                    previewWidth,
                    previewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    false);

            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }

        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        final long startTime = SystemClock.uptimeMillis();

                        ///////////////////////////////////////////////

                        Bitmap myCroppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);
                        final Canvas canvas = new Canvas(croppedBitmap);
                        //  canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
                        bm = BitmapFactory.decodeResource(getResources(), R.drawable.daisy_example);

                        if (hololensCommunication.hasNewImage()) {

                            Log.d("message", "Deserialization successful");
                            bm = hololensCommunication.getDeserializedImage();
                            showReceivedPicture(bm);

                            Bitmap mutableBitmap = bm.copy(Bitmap.Config.ARGB_8888, true);

                            Bitmap toRecognizeBitmap = Bitmap.createScaledBitmap(mutableBitmap, INPUT_SIZE, INPUT_SIZE, false);

                            final List<Classifier.Recognition> results = classifier.recognizeImage(toRecognizeBitmap);
                            ///////////////////////////////////////////////
                            //      final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);

                            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                            resultsView.setResults(results);
                            String url = baseAddress +
                                    "sendResult";
                            String result = results.get(0).getTitle();
                            String message = "{\"result\":\"" + result + "\"}";
                            hololensCommunication.sendToHoloLensPost(url, message);
                            Log.d("message", "post message sent to Holocommunication");
                            hololensCommunication.setNewImage(false);

                        }
                        requestRender();
                        computing = false;
                    }
                });

        Trace.endSection();
    }

    private void showReceivedPicture(Bitmap image) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pictureToRecognize.setImageBitmap(bm);
            }
        });


    }

    @Override
    public void onSetDebug(boolean debug) {
        classifier.enableStatLogging(debug);
    }

    private void renderDebug(final Canvas canvas) {
        if (!isDebug()) {
            return;
        }
        final Bitmap copy = cropCopyBitmap;
        if (copy != null) {
            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                    canvas.getWidth() - copy.getWidth() * scaleFactor,
                    canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (classifier != null) {
                String statString = classifier.getStatString();
                String[] statLines = statString.split("\n");
                for (String line : statLines) {
                    lines.add(line);
                }
            }

            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference time: " + lastProcessingTimeMs + "ms");

            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
        }
    }

    Runnable repeatingRunnable = new Runnable() {
        @Override
        public void run() {
            try {
              requestImageFromHololens();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                handler.postDelayed(repeatingRunnable, interval);
            }
        }
    };


    void startRepeatingTask() {
        repeatingRunnable.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(repeatingRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    @Override
    public void onResume() {
        super.onResume();
        mManager=OnyxBeaconApplication.getOnyxBeaconManager(this);
        contentReceiver = new ContentReceiver(this);
        mManager.setForegroundMode(true);
        registerReceiver(contentReceiver, new IntentFilter("bacon.accenture.com.beacon.content"));

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(contentReceiver);
        mManager.setForegroundMode(false);
    }

    @Override
    public void didRangeBeaconsInRegion(List<IBeacon> list) {
        for (IBeacon iBeacon:list){
            if (iBeacon.getProximity()==1){
                if (iBeacon.getMinor()==6689){
                    //dormitor
                    Log.e("In apropiere ","dormitor");

                    String text = "You have arrived to the reception area";
                    sendMessageToHololens(text);
                }else if (iBeacon.getMinor()==6657){
                    //baie
                    Log.e("In apropiere","baie");

                    String text = "You have arrived to the payments area";
                    sendMessageToHololens(text);
                }else if (iBeacon.getMinor()==6714){
                    //bucatarie
                    Log.e("In apropiere","bucatarie");
                    String url = baseAddress +
                            "sendText";
                    String text = "You have arrived to somewhere";
                    sendMessageToHololens(text);
                }
            }
        }
        Log.e("Beacons ",list.toString());
        Log.d("Beacons ",list.toString());
    }

    public void sendMessageToHololens(String message){
        if( !message.equals(textToSend)) {
            String url = baseAddress +
                    "sendText";
            hololensCommunication.sendToHoloLensPost(url, "{\"result\":\"" + message + "\"}");
            textToSend = message;
        }
    }

    @Override
    public void onError(int i, Exception e) {
        Log.e("Beacons ",e.toString());
        Log.d("Beacons ",e.toString());
    }

    @Override
    public void onBleStackEvent(int event) {
        Log.e("Beacons ", String.valueOf(event));
        Log.d("Beacons ", String.valueOf(event));
    }
}
