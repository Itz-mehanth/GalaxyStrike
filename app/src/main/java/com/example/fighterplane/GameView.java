package com.example.fighterplane;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private boolean isPlaying;
    private int screenWidth, screenHeight;
    private Bitmap planeBitmap, enemyBitmap, bulletBitmap,backgroundBitmap;
    private float planeX, planeY;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private long lastBulletTime;
    private float backgroundX; // Position of the background
    private boolean gameOver;
    private int score;// To keep track of points
    private SoundPool soundPool;
    private int bulletSoundId;
    private boolean isLoaded;



    private Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        System.out.println("decoding resources");
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Log original dimensions
        Log.d("BitmapDebug", "Original width: " + options.outWidth + ", height: " + options.outHeight);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Log sample size
        Log.d("BitmapDebug", "Sample size: " + options.inSampleSize);

        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);

        // Log scaled dimensions
        Log.d("BitmapDebug", "Scaled width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());

        return bitmap;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public GameView(Context context) {
        super(context);
        init(context);  // Initialize common properties
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);  // Initialize common properties
    }

    private void init(Context context) {
        // Your initialization code here
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        bulletSoundId = soundPool.load(context, R.raw.squick, 1);
        isLoaded = false;

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> isLoaded = true);

        lastBulletTime = System.currentTimeMillis();
        gameOver = false;
        score = 0;

        // Load bitmaps
        planeBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.plane, 60, 60);
        enemyBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.sword_fish, 50, 50);
        bulletBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.bullet, 10, 10);
        backgroundBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.oceanbackground, 3898, 2000); // Ensure this is 3 times the width of the screen
        backgroundX = 0; // Start background position at 0

        planeX = 500;
        planeY = 1500;
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();

        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                screenWidth = getWidth();
                screenHeight = getHeight();
                isPlaying = true;
                gameThread = new Thread(GameView.this);
                gameThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isPlaying = false;
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Method to play the sound when a bullet is shot
    private void playBulletSound() {
        if (isLoaded) {
            soundPool.play(bulletSoundId, 1, 1, 1, 0, 1f);
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!gameOver) {
                update();
            }
            draw();
            try {
                Thread.sleep(16); // approx 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        if (gameOver) {
            return;
        }

        // Update bullets
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            bullet.x += bullet.speedX; // Move the bullet to the right
            if (bullet.x > screenWidth) { // Remove bullets that move off the right side
                bullets.remove(i);
                i--;
            }
        }

        // Update enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.x -= 10; // Move the enemy to the left
            if (enemy.x + enemyBitmap.getWidth() < 0) { // Remove enemies that move off the left side
                enemies.remove(i);
                i--;
            }
        }

        // Check for collisions
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            for (Enemy enemy : enemies) {
                if (bullet.x < enemy.x + enemyBitmap.getWidth() &&
                        bullet.x + bulletBitmap.getWidth() > enemy.x &&
                        bullet.y < enemy.y + enemyBitmap.getHeight() &&
                        bullet.y + bulletBitmap.getHeight() > enemy.y) {

                    enemy.reduceHealth(bullet.attack);
                    bulletsToRemove.add(bullet);

                    // Check if the enemy is dead
                    if (enemy.isDead()) {
                        enemiesToRemove.add(enemy);
                    }
                }
            }
        }

        // Check for collision between plane and enemies
        for (Enemy enemy : enemies) {
            if (planeX < enemy.x + enemyBitmap.getWidth() &&
                    planeX + planeBitmap.getWidth() > enemy.x &&
                    planeY < enemy.y + enemyBitmap.getHeight() &&
                    planeY + planeBitmap.getHeight() > enemy.y) {
                gameOver = true;
                break;
            }
        }

        // Remove bullets and enemies
        bullets.removeAll(bulletsToRemove);
        enemies.removeAll(enemiesToRemove);

        // Spawn new enemies
        if (Math.random() < 0.03) {
            float enemyX = screenWidth + enemyBitmap.getWidth(); // Start off the right side
            float enemyY = (float) Math.random() * (screenHeight - enemyBitmap.getHeight());
            enemies.add(new Enemy("Enemy", enemyX, enemyY, 100)); // Example enemy with 100 health
        }

//        // Update background position
        backgroundX -= 5; // Adjust this value to control the speed of the scrolling
        if (backgroundX <= -backgroundBitmap.getWidth()) {
            backgroundX = 0; // Reset background position when it moves off-screen
        }
    }


    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawRGB(0, 0, 0);

            // Draw background
            canvas.drawBitmap(backgroundBitmap, backgroundX, 0, null);
            canvas.drawBitmap(backgroundBitmap, backgroundX + backgroundBitmap.getWidth(), 0, null);
            canvas.drawBitmap(backgroundBitmap, backgroundX + 2 * backgroundBitmap.getWidth(), 0, null);

            // Draw plane
            canvas.drawBitmap(planeBitmap, planeX, planeY, null);

            // Draw bullets
            for (Bullet bullet : bullets) {
                canvas.drawBitmap(bulletBitmap, bullet.x, bullet.y, null);
            }

            // Draw enemies
            for (Enemy enemy : enemies) {
                canvas.drawBitmap(enemyBitmap, enemy.x, enemy.y, null);
            }

            // Draw enemies and their health bars
            for (Enemy enemy : enemies) {
                canvas.drawBitmap(enemyBitmap, enemy.x, enemy.y, null);

                // Draw health bar
                Paint healthPaint = new Paint();
                healthPaint.setColor(Color.RED); // Full health color (background)
                float healthBarWidth = enemyBitmap.getWidth();
                float healthBarHeight = 10;
                float healthPercentage = (float) enemy.currentHealth / enemy.maxHealth;

                // Background (full health)
                canvas.drawRect(enemy.x, enemy.y - 20, enemy.x + healthBarWidth, enemy.y - 20 + healthBarHeight, healthPaint);

                // Foreground (current health)
                healthPaint.setColor(Color.GREEN); // Remaining health color
                canvas.drawRect(enemy.x, enemy.y - 20, enemy.x + healthBarWidth * healthPercentage, enemy.y - 20 + healthBarHeight, healthPaint);
            }

            // Draw Game Over message if game is over
            if (gameOver) {
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(100);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Game Over", screenWidth / 2, screenHeight / 2, paint);
            }

            // Draw score at the top-right corner
            Paint scorePaint = new Paint();
            scorePaint.setColor(Color.WHITE);
            scorePaint.setTextSize(50);
            canvas.drawText("Score: " + score, screenWidth - 200, 100, scorePaint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("GameView", "Touch event received: x=" + event.getX() + ", y=" + event.getY());

        // Update plane position based on touch event
        planeX = event.getX() - (float) planeBitmap.getWidth() / 2;
        planeY = event.getY() - (float) planeBitmap.getHeight() / 2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Shoot bullets while dragging
                synchronized (bullets) {
                    if (bullets.size() < 100) {
                        float bulletX = planeX + planeBitmap.getWidth() / 2;// Example limit
                        long currentTime = System.currentTimeMillis();  // Initialize current time
                        if (currentTime - lastBulletTime > 100) { // Shoot a bullet every 100 milliseconds
                            bullets.add(new Bullet(bulletX, planeY, 20, 20)); // Add horizontal speed
                            playBulletSound(); // Play sound on bullet release
                            lastBulletTime = currentTime; // Update last bullet time
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                // No additional shooting when the touch is lifted
                break;
        }

        return true;
    }



    // Bullet class with attack power and horizontal speed
    class Bullet {
        float x, y;
        int attack;
        float speedX; // Speed in the X direction

        Bullet(float x, float y, int attack, float speedX) {
            this.x = x;
            this.y = y;
            this.attack = attack;
            this.speedX = speedX;
        }
    }


    // Enemy class with health, name, and position
    class Enemy {
        String name;
        float x, y;
        int maxHealth, currentHealth;

        Enemy(String name, float x, float y, int maxHealth) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.maxHealth = maxHealth;
            this.currentHealth = maxHealth;
        }

        // Method to reduce health
        void reduceHealth(int damage) {
            currentHealth -= damage;
            if (currentHealth < 0) {
                currentHealth = 0;
            }
        }

        // Check if enemy is dead
        boolean isDead() {
            return currentHealth <= 0;
        }
    }
}