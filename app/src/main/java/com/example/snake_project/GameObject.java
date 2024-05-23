package com.example.snake_project;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BitmapFactory;


public interface GameObject {
    void draw(Canvas canvas, Paint paint);
    void update();
    void move();

}




