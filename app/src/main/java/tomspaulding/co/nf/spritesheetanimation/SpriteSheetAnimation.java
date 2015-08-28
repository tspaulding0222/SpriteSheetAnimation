package tomspaulding.co.nf.spritesheetanimation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpriteSheetAnimation extends Activity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize the GameView
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        gameView.pause();
    }

    class GameView extends SurfaceView implements Runnable{
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;

        Canvas canvas;
        Paint paint;

        long fps;
        private long timeThisFrame;

        Bitmap bitmapBob;
        boolean isMoving;
        float walkSpeedPerSecond = 250;
        float boxXPosition = 10;

        //sprite frame height and width
        //values can be anything as long as they dont distort the sprite
        private int frameWidth = 100;
        private int frameHeight = 50;

        //number of frames on the sprite sheet
        private int frameCount = 5;

        //current frame of sprite
        private int currentFrame = 0;

        //time when the last frame changed
        private long lastFrameChangeTime = 0;

        //how long shoud each frame last
        private int frameLenghtInMilliseconds = 100;

        //rect to define area of the sprite sheet that represents 1 frame
        private Rect frameToDraw = new Rect(0 , 0, frameWidth, frameHeight);

        //rect that defines an area of the screen on which to draw
        RectF whereToDraw = new RectF(boxXPosition, 0, boxXPosition + frameWidth, frameHeight);

        public GameView(Context context){
            super(context);

            ourHolder = getHolder();
            paint = new Paint();

            bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.bob);
            bitmapBob = Bitmap.createScaledBitmap(bitmapBob, frameWidth * frameCount, frameHeight, false);
        }

        @Override
        public void run() {
            while(playing){
                long startFrameTime = System.currentTimeMillis();

                update();

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame >=1){
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void getCurrentFrame(){

            long time = System.currentTimeMillis();

            if(isMoving){
                if(time > lastFrameChangeTime + frameLenghtInMilliseconds){
                    lastFrameChangeTime = time;
                    currentFrame ++;
                    if(currentFrame >= frameCount){
                        currentFrame = 0;
                    }
                }
            }

            frameToDraw.left = currentFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;
        }

        public void update(){
            if(isMoving){
                boxXPosition = boxXPosition + (walkSpeedPerSecond / fps);
            }
        }

        public void draw(){
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 26, 128, 182));

                paint.setColor(Color.argb(255, 249, 129, 0));

                paint.setTextSize(45);

                canvas.drawText("FPS:" + fps, 20, 40, paint);

                //draw bob
                whereToDraw.set((int) boxXPosition, 100, (int)boxXPosition + frameWidth, frameHeight + 100);
                getCurrentFrame();
                canvas.drawBitmap(bitmapBob, frameToDraw, whereToDraw, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause(){
            playing = false;
            try{
                gameThread.join();
            }
            catch(InterruptedException e){
                Log.e("Error:", "joining thread");
            }
        }

        public void resume(){
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch(motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;

                    break;
                case MotionEvent.ACTION_UP:
                    isMoving = false;

                    break;
            }

            return true;
        }
    }
}
