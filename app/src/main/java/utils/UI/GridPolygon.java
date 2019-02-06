package utils.UI;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

/**
 * Created by dmitry on 21.08.18.
 */

public class GridPolygon extends Polygon {

    private BitmapShader bitmapShader;
    private IGeoPoint lastCenterGeoPoint;
    private int xOffset = 0;
    private int yOffset = 0;
    private int height;
    private int width;

    public GridPolygon() {
        super();
    }

    public void setPatternBitmap(@NonNull final Bitmap patternBitmap) {
        height = patternBitmap.getHeight();
        width = patternBitmap.getWidth();
        bitmapShader = new BitmapShader(patternBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mFillPaint.setShader(bitmapShader);
    }

    protected void recalculateMatrix(@NonNull final MapView mapView) {
        final int zoomLevel = mapView.getZoomLevel();

        final Projection projection = mapView.getProjection();
        final IGeoPoint geoPoint = mapView.getMapCenter();
        if (lastCenterGeoPoint == null) lastCenterGeoPoint = geoPoint;

        /*
        Log.d("BitmapPolygon", "geoPoint.getLatitude() =" + geoPoint.getLatitude());
        Log.d("BitmapPolygon", "geoPoint.getLongitude() =" + geoPoint.getLongitude());
        Log.d("BitmapPolygon", "lastCenterGeoPoint.getLatitude() =" + lastCenterGeoPoint.getLatitude());
        Log.d("BitmapPolygon", "lastCenterGeoPoint.getLongitude() = " + lastCenterGeoPoint.getLongitude());
        */

        final Point point = projection.toPixels(geoPoint, null);
        final Point lastCenterPoint = projection.toPixels(lastCenterGeoPoint, null);

        /*
        Log.d("BitmapPolygon", "point.x = " + point.x);
        Log.d("BitmapPolygon", "point.y = " + point.y);
        Log.d("BitmapPolygon", "lastCenterPoint.x = " + lastCenterPoint.x);
        Log.d("BitmapPolygon", "lastCenterPoint.y = " + lastCenterPoint.y);
        */

        xOffset += lastCenterPoint.x - point.x;
        yOffset += lastCenterPoint.y - point.y;

        xOffset %= width;
        yOffset %= height;

        final Matrix matrix = new Matrix();
        matrix.reset();

        float scale = getScale(zoomLevel);

        //matrix.setScale(scale,scale);
        matrix.postScale(scale,scale);
        matrix.postTranslate(xOffset, yOffset);
        //matrix.setTranslate(xOffset, yOffset);
        bitmapShader.setLocalMatrix(matrix);

        /*
        Log.d("BitmapPolygon", "xOffset = " + xOffset);
        Log.d("BitmapPolygon", "yOffset = " + yOffset);
        */

        mFillPaint.setShader(bitmapShader);

        lastCenterGeoPoint = geoPoint;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (bitmapShader != null)
            recalculateMatrix(mapView);
        super.draw(canvas, mapView, shadow);
    }


    private float getScale(int zoomLevel) {
        float scale = 1f;
        final int MAX_ZOOM_LEVEL = 16;

        if (zoomLevel < MAX_ZOOM_LEVEL)
            scale = 1f /(MAX_ZOOM_LEVEL - zoomLevel);

        return scale;
    }


    @Override
    public boolean onSingleTapUp(MotionEvent e, MapView mapView) {
        if (this.isEnabled() == false)
            return true;

        if (e.getAction() == MotionEvent.ACTION_UP && contains(e)) {
            InfoWindow.closeAllInfoWindowsOn(mapView);
        }
        return super.onSingleTapConfirmed(e, mapView);
    }
}
