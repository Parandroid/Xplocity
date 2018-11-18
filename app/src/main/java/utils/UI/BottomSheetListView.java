package utils.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by dmitry on 08.10.17.
 */

public class BottomSheetListView extends ListView {

    public BottomSheetListView(Context context, AttributeSet p_attrs)
    {
        super(context, p_attrs);
    }

    private float mCurY;
    private float mPrevY;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mPrevY = ev.getY();
        }
        else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            mCurY = ev.getY();

            if (mPrevY != 0f) {
                float deltaY = mCurY - mPrevY;

                if (deltaY >= 0) {
                    // Swipe down
                    if (canScrollVertically(this)) {
                        getBottomSheetParent().requestDisallowInterceptTouchEvent(true);
                    }

                } else {
                    // Swipe up
                    getBottomSheetParent().requestDisallowInterceptTouchEvent(true);
                }
            }
        }
        else if (ev.getAction() == MotionEvent.ACTION_UP) {
            mPrevY = 0f;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        if (canScrollVertically(this))
        {
            getBottomSheetParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(ev);
    }

    public boolean canScrollVertically(AbsListView view)
    {
        boolean canScroll = false;

        if (view != null && view.getChildCount() > 0)
        {
            boolean isOnTop = view.getFirstVisiblePosition() != 0 || view.getChildAt(0).getTop() != 0;
            boolean isAllItemsVisible = isOnTop && view.getLastVisiblePosition() == view.getChildCount();

            if (isOnTop || isAllItemsVisible)
            {
                canScroll = true;
            }
        }

        return canScroll;
    }

    private ViewParent getBottomSheetParent() {
        return this.getParent();
    }



}
