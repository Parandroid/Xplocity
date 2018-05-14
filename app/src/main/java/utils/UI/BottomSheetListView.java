package utils.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.ListView;

import com.xplocity.xplocity.R;

/**
 * Created by dmitry on 08.10.17.
 */

public class BottomSheetListView extends ListView {

    public BottomSheetListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!canScrollVertically(this)) {
            getParent().requestDisallowInterceptTouchEvent(false);
            //getBottomSheetParent(this).requestDisallowInterceptTouchEvent(false);
        }
        else {
            getParent().requestDisallowInterceptTouchEvent(true);
            //getBottomSheetParent(this).requestDisallowInterceptTouchEvent(true);

        }

        return super.onInterceptTouchEvent(motionEvent);

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (canScrollVertically(this)) {
            getParent().requestDisallowInterceptTouchEvent(true);
            //getBottomSheetParent(this).requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(motionEvent);
    }

    public boolean canScrollVertically(AbsListView absListView) {

        boolean canScroll = false;

        if (absListView != null && absListView.getChildCount() > 0) {

            boolean isOnTop = absListView.getFirstVisiblePosition() != 0 || absListView.getChildAt(0).getTop() != 0;
            boolean isAllItemsVisible = isOnTop && getLastVisiblePosition() == absListView.getChildCount();

            if (isOnTop || isAllItemsVisible)
                canScroll = true;
        }

        return canScroll;
    }


    /*private ViewParent getBottomSheetParent(View view) {
        ViewParent parent = view.getParent();
        if (((View) parent).getId() == R.id.pager) {
            return parent;
        }
        else
        {
            return getBottomSheetParent((View) parent);
        }
    }*/
}
