package com.example.snake_project;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.Random;

public interface EntityObjects {
    //spawnObject_Random works when it gets its data

    void spawnObject_Random();
    void spawnObject_Precise(int x, int y, Boolean withEffect);
    default void LOADER_spawnObject_Random(Point location, Point SpawnRange){
        Random random = new Random();
        location.x = random.nextInt(SpawnRange.x) + 1;
        location.y = random.nextInt(SpawnRange.y - 1) + 1;
    }


    public void draw(Canvas canvas, Paint paint);
    public Point getLocation();
    public Point loadSpawnRange();
    public Point loadLocation();


}
