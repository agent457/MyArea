package com.example.myarea;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class RotatableImageView extends SubsamplingScaleImageView {

    private float currentRotation = 0f;

    public RotatableImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }
    public RotatableImageView(Context context) {
        super(context);
    }

    // Method to apply rotation
    public void setRotation(float angle) {
        this.currentRotation = angle;
        invalidate();
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        // Apply the rotation before drawing
        if (currentRotation != 0) {
            PointF center = getCenter(); // Get center of the image
            canvas.rotate(currentRotation, center.x, center.y);
        }
        super.onDraw(canvas); // Call the superclass draw method
    }

    // Reset rotation to default
    public void resetRotation() {
        this.currentRotation = 0f;
        invalidate();
    }
}
