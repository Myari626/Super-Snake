package com.example.snake_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.Random;

public abstract class NPC_EntityObjects {
    protected final Point mSpawnRange;
    protected final Point location;
    protected final int mSize;
    protected final Bitmap mBitmapObject;
    protected final Context context;

    protected NPC_EntityObjects(Context context, Point mSpawnRange,int mSize, int objectID) {
        Bitmap mBitmapObject1;
        this.context = context;
        this.mSpawnRange = mSpawnRange;
        this.mSize = mSize;
        this.location = new Point();
        mBitmapObject1 = BitmapFactory.decodeResource(context.getResources(), objectID);
        mBitmapObject1 = Bitmap.createScaledBitmap(mBitmapObject1, mSize, mSize, false);
        this.mBitmapObject = mBitmapObject1;
    }


    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(
                mBitmapObject,
                location.x*mSize,
                location.y*mSize,
                paint);
    }


    public void spawnObject_Random(){
        Random random = new Random();
        location.x = random.nextInt(mSpawnRange.x) + 1;
        location.y = random.nextInt(mSpawnRange.y - 1) + 1;
    };
    public void spawnObject_Percise(int x, int y, boolean withEffect){
        location.x = x;
        location.y = y;
        if (withEffect) {
        }
    };

    public Point getLocation(){
        return location;
    }

    public void move() {
    }

    public void update() {
    }


}
