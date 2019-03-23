package utils.UI;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.xplocity.xplocity.R;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.List;

import utils.ResourceGetter;

/**
 * Created by dmitry on 21.08.18.
 */

public class GridPolygon extends Polygon {

    private BitmapShader bitmapShader;
    private BitmapShader bitmapShaderZoom;
    private IGeoPoint lastCenterGeoPoint;
    private int xOffset = 0;
    private int yOffset = 0;
    private int height;
    private int width;




    public GridPolygon() {
        super();
    }

    @Override
    public void setPoints(final List<GeoPoint> points) {
        super.setPoints(points);

        calculateBoundings();
    }

    public void setPatternBitmap(@NonNull final Bitmap patternBitmap) {
        height = patternBitmap.getHeight();
        width = patternBitmap.getWidth();
        bitmapShader = new BitmapShader(patternBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        //Bitmap bitmap = BitmapFactory.decodeResource(ResourceGetter.getResources(),R.drawable.ic_question);
        //bitmapShaderZoom = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mFillPaint.setShader(bitmapShader);
    }

    protected void recalculateMatrix(@NonNull final MapView mapView) {
        final int zoomLevel = mapView.getZoomLevel();

        final Projection projection = mapView.getProjection();
        final IGeoPoint geoPoint = mapView.getMapCenter();
        if (lastCenterGeoPoint == null) lastCenterGeoPoint = geoPoint;

        final Point point = projection.toPixels(geoPoint, null);
        final Point lastCenterPoint = projection.toPixels(lastCenterGeoPoint, null);

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

        mFillPaint.setShader(bitmapShader);

        lastCenterGeoPoint = geoPoint;
    }


    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;


    private void calculateBoundings() {
        GeoPoint fp = this.getPoints().get(0);

        minLat = fp.getLatitude();
        maxLat = fp.getLatitude();
        minLon = fp.getLongitude();
        maxLon = fp.getLongitude();

        for (GeoPoint p:this.getPoints()) {
            if (p.getLatitude() < minLat)
                minLat = p.getLatitude();
            if (p.getLatitude() > maxLat)
                maxLat = p.getLatitude();
            if (p.getLongitude() < minLon)
                minLon = p.getLongitude();
            if (p.getLongitude() > maxLon)
                maxLon = p.getLongitude();
        }
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (bitmapShader != null) {
            if (mapView.getZoomLevel() < 14) {
                //mFillPaint.setShader(bitmapShaderZoom);

                mFillPaint.setShader(null);
                this.setFillColor(ResourceGetter.getResources().getColor(R.color.transparent));
                Projection projection = mapView.getProjection();
                Point tl = new Point();
                Point br = new Point();
                projection.toPixels(new GeoPoint(maxLat, minLon), tl);
                projection.toPixels(new GeoPoint(minLat, maxLon), br);

                Drawable d = ResourceGetter.getResources().getDrawable(R.drawable.seekbar_thumb);
                d.setBounds(tl.x, tl.y, br.x, br.y);
                d.draw(canvas);
                //
            }
            else {
                mFillPaint.setShader(bitmapShader);
                this.setFillColor(ResourceGetter.getResources().getColor(R.color.black));
                recalculateMatrix(mapView);
            }
        }

        super.draw(canvas, mapView, shadow);
    }


    private float getScale(int zoomLevel) {
        float scale = 1f;
        final int MAX_ZOOM_LEVEL = 15;

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
