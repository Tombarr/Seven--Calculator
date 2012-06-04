package com.tombarrasso.android.wp7calculator;

/*
 * Flip3DAnimation.java
 *
 * Copyright (C) Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Android Packages
// import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

// Tikuwarez Packages
import tikuwarez.graphics.L7Camera;

/**
 * Animation used to flip image in 3D.
 */

public final class Flip3DAnimation extends Animation
{
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private L7Camera mCamera;

    public Flip3DAnimation(final float fromDegrees, final float toDegrees, final float centerX, final float centerY)
    {
        super();
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
    }

    @Override
    public void initialize(final int width, final int height, final int parentWidth, final int parentHeight)
    {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new L7Camera();
        
        // Lets go Silverlight plane!
        mCamera.setLocation(0, 0, -56);
    }

    @Override
    protected void applyTransformation(final float interpolatedTime, final Transformation t)
    {
        final float fromDegrees = mFromDegrees;
        final float degrees = fromDegrees + (mToDegrees - fromDegrees) * interpolatedTime;

        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final L7Camera camera = mCamera;

        final Matrix matrix = t.getMatrix();

        camera.save();

        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}
