package com.example.snake_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

class Apple implements GameObject{


    // The location of the apple on the grid
    // Not in pixels
    private Point location = new Point();

    // The range of values we can choose from
    // to spawn an apple
    private Point mSpawnRange;
    private int mSize;

    // An image to represent the apple
    private Bitmap mBitmapApple;

    private Bitmap mBitmapBadApple;

    private boolean isBad;

    /// Set up the apple in the constructor
    Apple(Context context, Point sr, int s){

        // Make a note of the passed in spawn range
        mSpawnRange = sr;
        // Make a note of the size of an apple
        mSize = s;


        // Load the image to the bitmap
        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);

        mBitmapBadApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.badapple);

        // Resize the bitmap
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);

        mBitmapBadApple = Bitmap.createScaledBitmap(mBitmapBadApple, s, s, false);
    }

    // This is called every time an apple is eaten
    void spawn(){
        // Choose two random values and place the apple
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;

        // 20% chance of spawning a bad apple
        isBad = random.nextInt(5) == 0;
    }
    // Overloaded spawn method with specific coordinates
    void spawn(int x, int y, boolean withEffect) {
        location.x = x;
        location.y = y;
        if (withEffect) {
        }
    }

    // Let SnakeGame know where the apple is
    // SnakeGame can share this with the snake
    public Point getLocation() {
        return location;
    }


    // Draw the apple
    @Override
    public void draw(Canvas canvas, Paint paint) {
        // If the apple is bad, draw the bad apple
        if (isBad) {
            canvas.drawBitmap(mBitmapBadApple,
                    location.x * mSize, location.y * mSize, paint);
        }
        // If the apple is good, draw the good apple
        else {
            canvas.drawBitmap(mBitmapApple,
                    location.x * mSize, location.y * mSize, paint);
        }
    }

    @Override
    public void update() {
        // Apple's update might be related to animations or effects.
    }
    @Override
    public void move() {
        // Since apples don't move, you can leave this method empty.
    }

    public boolean isBad() {
        return isBad;
    }

}