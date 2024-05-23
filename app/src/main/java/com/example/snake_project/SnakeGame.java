package com.example.snake_project;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.graphics.Rect;
class SnakeGame extends SurfaceView implements Runnable, Clickables{

    // Objects for the game loop/thread
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    private int mScore;

    private int mTotalScore = 0;

    private int mHighScore = 0;

    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    private Snake mSnake;
    private Apple mApple;
    private ObstacleObject mObstacle;

    private Bitmap mBackgroundBitmap;
    private int appleEatenCount = 0;
    private Bitmap mPauseButtonBitmap;
    private Rect mPauseButtonRect;
    private int buttonSize;
    private int buttonPosX;
    private int buttonPosY;

    private Bitmap mCrewmateBitmap;
    private Rect mCrewmateRect;
    private Bitmap mSnakeBitmap;
    private Rect mSnakeRect;
    private Bitmap mUfoBitmap;
    private Rect mUfoRect;

    private int nova_blue = Color.argb(225, 0, 225, 200);
    private int jungle_green = Color.argb(235, 0, 235, 160);
    int blue = Color.argb(255, 0, 0, 255);
    protected Point size;


    AssetManager assetManager;
    AssetFileDescriptor descriptor;
    // This is the constructor method that gets called
    // from SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;

        // Initialize the SoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        // Set initial sounds in memory
        assetManager = context.getAssets();
        changeSounds("get_apple.ogg", "snake_death.ogg");

        // Initialize the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        // Call the constructors of our two game objects
        mApple = new Apple(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mSnake = new Snake(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mObstacle = new ObstacleObject(context, new Point(NUM_BLOCKS_WIDE,
                mNumBlocksHigh),
                blockSize);

        drawPauseButton(size);
        drawShopButton(size);
    }
    private void drawPauseButton(Point size){
        mPauseButtonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pause); // Ensure this is the correct resource name
        clickableSize(size, 20); // Adjust the size based on your screen width for responsiveness
        mPauseButtonBitmap = Bitmap.createScaledBitmap(mPauseButtonBitmap, buttonSize, buttonSize, false);
        clickablePosition(size, 50, 50); // Position the pause button on the bottom right of the screen
        mPauseButtonRect = new Rect(buttonPosX, buttonPosY, buttonPosX + buttonSize, buttonPosY + buttonSize);
    }
    private void drawShopButton(Point size){
        mCrewmateBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.red); // Ensure this is the correct resource name
        clickableSize(size, 15); // Adjust the size based on your screen width for responsiveness
        mCrewmateBitmap = Bitmap.createScaledBitmap(mCrewmateBitmap, buttonSize, buttonSize, false);
        clickablePosition(size, 1425, 280); // Position the pause button on the bottom right of the screen
        mCrewmateRect = new Rect(buttonPosX, buttonPosY, buttonPosX + buttonSize, buttonPosY + buttonSize);

        mSnakeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head); // Ensure this is the correct resource name
        clickableSize(size, 20); // Adjust the size based on your screen width for responsiveness
        mSnakeBitmap = Bitmap.createScaledBitmap(mSnakeBitmap, buttonSize, buttonSize, false);
        clickablePosition(size, 1500, 80); // Position the pause button on the bottom right of the screen
        mSnakeRect = new Rect(buttonPosX, buttonPosY, buttonPosX + buttonSize, buttonPosY + buttonSize);

        mUfoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ufo); // Ensure this is the correct resource name
        clickableSize(size, 20); // Adjust the size based on your screen width for responsiveness
        mUfoBitmap = Bitmap.createScaledBitmap(mUfoBitmap, buttonSize, buttonSize, false);
        clickablePosition(size, 1600, 180); // Position the pause button on the bottom right of the screen
        mUfoRect = new Rect(buttonPosX, buttonPosY, buttonPosX + buttonSize, buttonPosY + buttonSize);
    }
    @Override
    public void clickableSize(Point size, int dividend) {
        buttonSize = size.x / dividend;
    }

    @Override
    public void clickablePosition(Point size, int posX, int posY) {
        buttonPosX = size.x - buttonSize - posX;
        buttonPosY = size.y - buttonSize - posY;
    }

    private void changeSounds(String getAppleSound, String deathSound) {
        try {
            AssetFileDescriptor descriptor;

            // Set initial sounds in memory
            descriptor = assetManager.openFd(getAppleSound);
            mEat_ID = mSP.load(descriptor, 0);
            descriptor = assetManager.openFd(deathSound);
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Error
            System.out.println("error in snake death");
        }
    }

    private enum GameState {
        INITIAL, RUNNING, PAUSED, GAME_OVER
    }

    private GameState gameState = GameState.INITIAL; // Start with the INITIAL state



    // Called to start a new game
    public void newGame() {

        // reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh, Snake.Heading.LEFT);

        // Get the apple ready for dinner
        mApple.spawn();
        mApple.spawn();
        mObstacle.spawn();

        // Reset the mScore
        mScore = 0;
        appleEatenCount = 0;


        // Setup mNextFrameTime so an update can triggered
        mNextFrameTime = System.currentTimeMillis();
    }

    public void onSnakeDeath() {
        gameState = GameState.GAME_OVER;
        // This is a hypothetical method you might have for handling snake death
        mPaused = true; // Pause the game
    }

    // Handles the game loop
    @Override
    public void run() {
        while (mPlaying) {
            if (!mPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    updateGameObjects();
                }
            }
            drawGameObjects();
        }
    }

    // Check to see if it is time for an update
    public boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }


    // Update all the game objects
    private void updateGameObjects() {

        // Move the snake
        mSnake.move();

        if(mSnake.checkDinner(mApple.getLocation())){
            appleEatenCount++; // Increment the apple eaten count

            if (mApple.isBad()) {
                handleBadAppleEaten();
            }
            else {
                mScore++;
            }

            // Play a sound
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);

            if (appleEatenCount % 10 == 0) {
                // Call method to spawn the apple further from the snake
                spawnAppleFurtherFromSnake();
                //mObstacle.spawnObstacleAway(mSnake.getHeadLocation());
                mObstacle.spawnObject_Random();
            } else {
                // Regular apple spawn
                mApple.spawn();
                mObstacle.spawnObject_Random();
            }
        }
        else if(mSnake.checkDinner(mObstacle.getLocation())){
            mPaused = true;
            scoreHighAndTotal();
            onSnakeDeath();
        }

        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);

            scoreHighAndTotal();

            mPaused = true;
            onSnakeDeath();

        }
    }
    private void scoreHighAndTotal(){
        mTotalScore += mScore;
        if (mHighScore < mScore) {
            mHighScore = mScore;
        }
    }

    private void handleBadAppleEaten() {
        // Deduct points from the player's score
        mScore -= 1;

        // Play a sound
        mSP.play(mCrashID, 1, 1, 0, 0, 1);
    }

    private void spawnAppleFurtherFromSnake() {
        Random random = new Random();
        Point newLocation;
        Point headLocation = mSnake.getHeadLocation(); // Get the snake's head location

        if (headLocation == null) {
            // Handle this unlikely case (e.g., by spawning the apple randomly)
            mApple.spawn();
            return;
        }

        do {
            int x = random.nextInt(NUM_BLOCKS_WIDE);
            int y = random.nextInt(mNumBlocksHigh);
            newLocation = new Point(x, y);
        } while (!isLocationFarEnough(newLocation, headLocation));

        mApple.spawn(newLocation.x, newLocation.y, true);
    }


    private boolean isLocationFarEnough(Point appleLocation, Point snakeHeadLocation) {
        // Define a minimum distance for the apple to be considered "far enough" from the snake's head.
        int minDistance = 5;
        int distance = Math.abs(appleLocation.x - snakeHeadLocation.x) + Math.abs(appleLocation.y - snakeHeadLocation.y);
        return distance >= minDistance;
    }

    // Do all the drawing
    private void drawGameObjects() {
        // Get a lock on the mCanvas
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();

            // Fill the screen with a color
            mCanvas.drawBitmap(mBackgroundBitmap, 0, 0, null);

            //draw the score
            textHandler(60,"Current Score: " + mScore, 20, 120, jungle_green);
            textHandler(60,"Total points: " + mTotalScore, 20, 70, jungle_green);
            textHandler(60, "High Score: " + mHighScore, 20, 170, jungle_green);

            // Draw the apple and the snake
            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);
            mObstacle.draw(mCanvas,mPaint);

            mCanvas.drawBitmap(mPauseButtonBitmap, mPauseButtonRect.left, mPauseButtonRect.top, null);

            // Draw some text while paused
            if (gameState == GameState.INITIAL) {

                textHandler(200, "Tap To Play",500, 600,nova_blue);
                textHandler(60,"Julian Flores, Mursal Yari", 1400, 50, jungle_green);
                textHandler(60,"Evan Callejo, Alan Lei", 1400, 100, jungle_green);
                textHandler(60,"Nicholas Sanchez", 1400, 150, jungle_green);

            } else if (gameState == GameState.GAME_OVER) {
                //text drawn of the game is over, ha you died
                textHandler(120,"Game Over!", 100, 300, nova_blue);
                textHandler(120,"Tap to Play Again", 100, 400, nova_blue);

                //Displays the shop menu
                textHandler(120,"Shop Menu", 100, 600, nova_blue);
                textHandler(60,"Crewmate Skin (8 pts): ", 100, 700, nova_blue);
                textHandler(60,"Ufo Skin (10 pts): ", 100, 800, nova_blue);
                textHandler(60,"Original Skin (4 pts): ", 100, 900, nova_blue);
                mCanvas.drawBitmap(mCrewmateBitmap, mCrewmateRect.left, mCrewmateRect.top, null);
                mCanvas.drawBitmap(mSnakeBitmap, mSnakeRect.left, mSnakeRect.top, null);
                mCanvas.drawBitmap(mUfoBitmap, mUfoRect.left, mUfoRect.top, null);

            } else if (gameState == GameState.PAUSED) {

                textHandler(200,"Game Paused!", 500, 600,nova_blue);
                mPaint.setTextSize(200);
            }

            // Unlock the mCanvas and reveal the graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();

        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            // If the game is in INITIAL or GAME_OVER state, tapping anywhere starts the game
            if (gameState == GameState.INITIAL || gameState == GameState.GAME_OVER) {
                gameState = GameState.RUNNING;
                mPaused = false;
                mPlaying = true;
                newGame();
                // Make sure to start the thread only if it's not already running
                if (mThread == null || !mThread.isAlive()) {
                    mThread = new Thread(this);
                    mThread.start();
                }
                return true; // Indicate that we've handled the touch event
            } else if (gameState == GameState.PAUSED || (mPauseButtonRect.contains(x, y) && gameState == GameState.RUNNING)) {
                // If the game is PAUSED, or the pause button is pressed while RUNNING, toggle pause
                mPaused = !mPaused;
                gameState = mPaused ? GameState.PAUSED : GameState.RUNNING;
                return true; // Indicate that we've handled the touch event
            }
        }

        // Determine if user can purchase item
        if (mCrewmateRect.contains(x, y) && gameState == GameState.GAME_OVER
                && mTotalScore >= 8 && mSnake.getDrawableHead() != R.drawable.red) {
            mSnake.changeDrawableHead(R.drawable.red);
            mSnake.changeDrawableBody(R.drawable.red);
            changeSounds("crewmate_sound_1.mp3", "crewmate_sound_2.mp3");
            mTotalScore -= 8;
            return true; // Indicate that we've handled the touch event
        } else if (mUfoRect.contains(x, y) && gameState == GameState.GAME_OVER
                && mTotalScore >= 10 && mSnake.getDrawableHead() != R.drawable.ufo) {
            mSnake.changeDrawableHead(R.drawable.ufo);
            mSnake.changeDrawableBody(R.drawable.invisible);
            changeSounds("get_apple.ogg", "snake_death.ogg");
            mTotalScore -= 10;
            return true; // Indicate that we've handled the touch event
        } else if (mSnakeRect.contains(x, y) && gameState == GameState.GAME_OVER
                && mTotalScore >= 4 && mSnake.getDrawableHead() != R.drawable.head) {
            mSnake.changeDrawableHead(R.drawable.head);
            mSnake.changeDrawableBody(R.drawable.body);
            changeSounds("get_apple.ogg", "snake_death.ogg");
            mTotalScore -= 4;
            return true; // Indicate that we've handled the touch event
        }

        // If the game is running and not paused, process snake direction changes
        if (!mPaused && gameState == GameState.RUNNING && motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mSnake.switchHeading(motionEvent);
        }

        return true; // Always return true to indicate that the touch event was handled
    }

    // Stop the thread
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }


    // Start the thread
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }


    //Allows the ability to change text size but use default color, NOTE x and y use INT
    public void textHandler(int textSizeSet, String text, int x, int y) {
        mPaint.setColor(blue);
        mPaint.setTextSize(textSizeSet);
        mCanvas.drawText(text, x, y, mPaint);
    }

    //Allows the ability to change text size but use default color, NOTE x and y use float
    public void textHandler(int textSizeSet, String text, float x, float y) {
        mPaint.setColor(blue);
        mPaint.setTextSize(textSizeSet);
        mCanvas.drawText(text, x, y, mPaint);

    }
    //allows full customization of the text size and color
    public void textHandler(int textSizeSet, String text, int x, int y, int colorSet) {
        mPaint.setColor(colorSet);
        mPaint.setTextSize(textSizeSet);
        mCanvas.drawText(text, x, y, mPaint);
    }
}
