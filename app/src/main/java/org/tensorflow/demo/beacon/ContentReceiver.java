package org.tensorflow.demo.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.onyxbeacon.OnyxBeaconApplication;
import com.onyxbeacon.listeners.OnyxBeaconsListener;
import com.onyxbeacon.listeners.OnyxCouponsListener;
import com.onyxbeacon.listeners.OnyxPushListener;
import com.onyxbeacon.listeners.OnyxTagsListener;
import com.onyxbeacon.model.Tag;
import com.onyxbeacon.model.web.BluemixApp;
import com.onyxbeacon.rest.model.Coupon;
import com.onyxbeaconservice.IBeacon;

import java.util.ArrayList;

public class ContentReceiver extends BroadcastReceiver {

    private OnyxBeaconsListener mOnyxBeaconListener;
    private OnyxCouponsListener mOnyxCouponsListener;
    private OnyxTagsListener mOnyxTagsListener;
    private OnyxPushListener mOnyxPushListener;
    private Gson gson = new Gson();
    private static ContentReceiver sInstance;

    public static ContentReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new ContentReceiver();
            return sInstance;
        } else {
            return sInstance;
        }
    }

    public ContentReceiver() {}

    public ContentReceiver(OnyxBeaconsListener onyxBeaconSDKListener) {
        mOnyxBeaconListener = onyxBeaconSDKListener;
    }

    public void setOnyxBeaconsListener(OnyxBeaconsListener onyxBeaconListener) {
        mOnyxBeaconListener = onyxBeaconListener;
    }

    public void setOnyxCouponsListener(OnyxCouponsListener onyxCouponsListener) {
        mOnyxCouponsListener = onyxCouponsListener;
    }

    public void setOnyxTagsListener(OnyxTagsListener onyxTagsListener){
        mOnyxTagsListener = onyxTagsListener;
    }

    public void setOnyxPushListener(OnyxPushListener onyxPushListener) {
        mOnyxPushListener = onyxPushListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String payloadType = intent.getStringExtra(OnyxBeaconApplication.PAYLOAD_TYPE);
        switch (payloadType) {
            case OnyxBeaconApplication.TAG_TYPE:
                ArrayList<Tag> tagsList = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_TAGS);
                if (mOnyxTagsListener != null) {
                    mOnyxTagsListener.onTagsReceived(tagsList);
                } else {
                    // In background display notification
                }
                break;
            case OnyxBeaconApplication.BEACON_TYPE:
                ArrayList<IBeacon> beacons = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_BEACONS);
                if (mOnyxBeaconListener != null) {
                    mOnyxBeaconListener.didRangeBeaconsInRegion(beacons);
                } else {
                    // In background display notification
                }
                break;
            case OnyxBeaconApplication.COUPON_TYPE:
                ArrayList<Coupon> coupons = intent.getParcelableArrayListExtra(OnyxBeaconApplication.EXTRA_COUPONS);
                IBeacon beacon = intent.getParcelableExtra(OnyxBeaconApplication.EXTRA_BEACON);

                if (mOnyxCouponsListener != null) {
                    mOnyxCouponsListener.onCouponsReceived(coupons, beacon);
                } else {
                    // In background display coupon
                }
                break;

            case OnyxBeaconApplication.PUSH_TYPE:
                BluemixApp bluemixApp = intent.getParcelableExtra(OnyxBeaconApplication.EXTRA_BLUEMIX);
                System.out.println("PUSH Received bluemix credentials " + gson.toJson(bluemixApp));
                if (mOnyxPushListener != null) {
                    mOnyxPushListener.onBluemixCredentialsReceived(bluemixApp);
                }
                break;
        }
    }

}
