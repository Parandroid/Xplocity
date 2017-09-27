package utils.UI;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

/**
 * Created by dmitry on 27.09.17.
 */

public class WaitWheel {

    FrameLayout mWaitWheel;
    Context mContext;
    AlphaAnimation mInAnimation;

    public WaitWheel(FrameLayout waitWheel, Context context) {
        mWaitWheel = waitWheel;
        mContext = context;

        mInAnimation = new AlphaAnimation(0f, 1f);
        mInAnimation.setDuration(200);
        mWaitWheel.setAnimation(mInAnimation);
    }

    public void showWaitAnimation() {
        mWaitWheel.setVisibility(View.VISIBLE);
        ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideWaitAnimation() {
        mWaitWheel.setVisibility(View.GONE);
        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
