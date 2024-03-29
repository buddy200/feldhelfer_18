package de.uni_stuttgart.informatik.sopra.fieldManager.UI.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.annotation.NonNull;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;

import de.uni_stuttgart.informatik.sopra.fieldManager.data.AgrarianField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.DamageField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.Field;

/**
 * sopra_priv
 * Created by Felix B on 19.11.17.
 * Mail: felix.burk@gmail.com
 *
 * A custom Polygon overlay for our MapView
 */

public class FieldPolygon extends Polygon {

    private Context context;
    private Paint textPaint;
    private Field field;
    private Point polyCentroidPoint;
    private BitmapShader bitmapShader;
    private IGeoPoint lastCenterGeoPoint;
    private int xOffset = 0;
    private int yOffset = 0;

    FieldPolygon(Context context, Field field) {
        super(context);

        this.field = field;
        this.context = context;

        //init default values
        this.setTitle("");

        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * overwriting the normal draw method, to display the name
     * in the center of gravity of the polygon
     * (this is the quickest way to get the title in a good position relative to the polygon
     * better would be a center point inside of it, but this might be a bit more difficult)
     * <p>
     * TODO: instead of a color use tile overlays from .png files
     *
     * @param canvas
     * @param mapView
     * @param shadow
     */
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if(shadow) return;

        //only draw names if zoomed in to certain level
        //TODO: show name depending to polygon size and zoom level
        if (field instanceof AgrarianField) {
            if (mapView.getZoomLevel() < 18) {
                if(bitmapShader != null) recalculateMatrix(mapView);
                super.draw(canvas, mapView, shadow);
                return;
            }
            textPaint.setTextSize(50);
            textPaint.setColor(Color.BLACK);

            //handle damage fields
        } else if (field instanceof DamageField) {
            if (mapView.getZoomLevel() < 19) {
                if(bitmapShader != null) recalculateMatrix(mapView);
                super.draw(canvas, mapView, shadow);
                return;
            }
            textPaint.setTextSize(40);
            textPaint.setColor(Color.BLACK);
        }

        polyCentroidPoint = new Point();
        if(bitmapShader != null) recalculateMatrix(mapView);

        super.draw(canvas, mapView, shadow);

        //after super call to draw names on top of everything else
        mapView.getProjection().toPixels(field.getCentroid(), polyCentroidPoint);
        canvas.drawText(this.getTitle(), polyCentroidPoint.x, polyCentroidPoint.y, textPaint);

    }

    /**
     * set bitmap pattern to be displayed inside polygon
     * @param patternBMP
     */
    void setPatternBMP(@NonNull final Bitmap patternBMP) {
        bitmapShader = new BitmapShader(patternBMP, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mFillPaint.setShader(bitmapShader);
    }

    /**
     * unused border triangle effect
     * @param strokeWidth
     * @return
     */
    private PathEffect getTrianglePathEffect(int strokeWidth) {
        return new PathDashPathEffect(
                getTriangle(strokeWidth),
                strokeWidth,
                0.0f,
                PathDashPathEffect.Style.ROTATE);
    }

    /**
     * for custom border triangle effect
     * @param size
     * @return
     */
    private Path getTriangle(float size) {
        Path path = new Path();
        float half = size / 2;
        path.moveTo(-half, -half);
        path.lineTo(half, -half);
        path.lineTo(0, half);
        path.close();
        return path;
    }

    /**
     * shift polygon content depending on camera position on map canvas
     * used to prevent scrolling bitmap patterns
     * @param mapView
     */
    private void recalculateMatrix(@NonNull final MapView mapView) {
        //final int mapSize = TileSystem.MapSize(mapView.getZoomLevel());

        final Projection projection = mapView.getProjection();
        final IGeoPoint geoPoint = mapView.getMapCenter();
        if (lastCenterGeoPoint == null) lastCenterGeoPoint = geoPoint;

        final Point point = projection.toPixels(geoPoint, null);
        final Point lastCenterPoint = projection.toPixels(lastCenterGeoPoint, null);

        xOffset += lastCenterPoint.x - point.x;
        yOffset += lastCenterPoint.y - point.y;

        xOffset %= 35; // 100 is pixel size of shader image
        yOffset %= 35;

        final Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setScale(1,1);
        matrix.preTranslate(xOffset, yOffset);
        //matrix.setTranslate(xOffset, yOffset);
        bitmapShader.setLocalMatrix(matrix);

        mFillPaint.setShader(bitmapShader);

        lastCenterGeoPoint = geoPoint;
    }
}