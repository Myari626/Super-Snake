package com.example.snake_project;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.Random;

public class ObstacleObject implements EntityObjects {
    private Point mSpawnRange;
    private Point location;
    private int mSize;
    private Bitmap mBitmapObject;
    private Context context;

    public ObstacleObject(Context context, Point mSpawnRange,int mSize) {
        Bitmap mBitmapObject1;
        this.context = context;
        this.mSpawnRange = mSpawnRange;
        this.mSize = mSize;
        this.location = new Point();
        mBitmapObject1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.obstacle);
        mBitmapObject1 = Bitmap.createScaledBitmap(mBitmapObject1, mSize, mSize, false);
        this.mBitmapObject = mBitmapObject1;
    }
    void spawn(){
        // Choose two random values and place the apple
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;
    }
    public void spawnObstacleAway(Point headLocation) {
        Random random = new Random();
        Point newLocation;
        int mNumBlocksHigh = location.y;

        if (headLocation == null) {
            // Handle this unlikely case (e.g., by spawning the apple randomly)
            spawnObject_Random();
            return;
        }

        do {
            int x = random.nextInt(40);
            int y = random.nextInt(mNumBlocksHigh);
            newLocation = new Point(x, y);
        } while (!isLocationFarEnough(newLocation, headLocation));

        spawnObject_Precise(newLocation.x, newLocation.y, true);
    }


    private boolean isLocationFarEnough(Point appleLocation, Point snakeHeadLocation) {
        // Define a minimum distance for the apple to be considered "far enough" from the snake's head.
        int minDistance = 5;
        int distance = Math.abs(appleLocation.x - snakeHeadLocation.x) + Math.abs(appleLocation.y - snakeHeadLocation.y);
        return distance >= minDistance;
    }
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(
                mBitmapObject,
                location.x*mSize,
                location.y*mSize,
                paint);
    }
    @Override
    public void spawnObject_Random() {
        LOADER_spawnObject_Random(location,mSpawnRange);
    }

    @Override
    public void spawnObject_Precise(int x, int y, Boolean withEffect) {
        location.x = x;
        location.y = y;
        if (withEffect) {
        }
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public Point loadSpawnRange() {
        return mSpawnRange;
    }
    @Override
    public Point loadLocation() {
        return location;
    }



}
