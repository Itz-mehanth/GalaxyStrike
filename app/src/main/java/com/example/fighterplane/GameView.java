package com.example.fighterplane;

import android.app.Activity;
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
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private boolean isPlaying;
    private int screenWidth, screenHeight;
    private Bitmap planeBitmap, enemyBitmap, bulletBitmap,backgroundBitmap,powerUpBitmap,masterEnemyBitmap;
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
    private static final int MAX_BULLETS = 100;
    private static final long RELOAD_TIME_MS = 4000; // 4 seconds

    private List<PowerUp> powerUps;

    private int bulletsFired = 0;
    private boolean isReloading = false;
    private long reloadStartTime = 0;

    private Paint progressBarPaint;
    private Paint progressBarBackgroundPaint;

    private MasterEnemy masterEnemy;


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
        // Load the power-up bitmap (adjust dimensions as needed)
        powerUpBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.powerup, 15, 15);
        masterEnemyBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.bullet, 150, 150);

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


        // Initialize power-ups list
        powerUps = new ArrayList<>();

        initPaint();
    }

    private void spawnPowerUp() {
        if (Math.random() < 0.02) { // 1% chance of spawning a power-up every frame
            float powerUpX = screenWidth + powerUpBitmap.getWidth(); // Start off the right side
            float powerUpY = (float) Math.random() * (screenHeight - powerUpBitmap.getHeight());

            String[] types = {"fasterShooting"}; // Different types of power-ups
            String randomType = types[(int) (Math.random() * types.length)];

            powerUps.add(new PowerUp(powerUpBitmap, powerUpX, powerUpY, randomType));
        }
    }

    private void updatePowerUps() {
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.x -= 10; // Move the power-up to the left

            if (powerUp.x + powerUp.bitmap.getWidth() < 0) {
                powerUps.remove(i);
                i--;
            }
        }
    }

    private void checkPowerUpCollision() {
        List<PowerUp> collectedPowerUps = new ArrayList<>();

        for (PowerUp powerUp : powerUps) {
            if (planeX < powerUp.x + powerUp.bitmap.getWidth() &&
                    planeX + planeBitmap.getWidth() > powerUp.x &&
                    planeY < powerUp.y + powerUp.bitmap.getHeight() &&
                    planeY + planeBitmap.getHeight() > powerUp.y) {

                collectedPowerUps.add(powerUp); // Collect the power-up
                applyPowerUpEffect(powerUp);    // Apply the power-up effect
            }
        }

        powerUps.removeAll(collectedPowerUps); // Remove collected power-ups
    }

    private void applyPowerUpEffect(PowerUp powerUp) {
        score += 100;
        switch (powerUp.type) {
            case "extraBullets":
                bulletsFired = 0; // Reset bullet count, allowing more bullets to be fired
                break;
            case "invincibility":
                // Make the player invincible for a short time
//                becomeInvincible();
                break;
            case "fasterShooting":
                // Increase the shooting speed temporarily
                lastBulletTime -= 500; // Reduce time between shots
                break;
        }
    }

    private void drawPowerUps(Canvas canvas) {
        for (PowerUp powerUp : powerUps) {
            canvas.drawBitmap(powerUp.bitmap, powerUp.x, powerUp.y, null);
        }
    }

    void createMaster(){
        // Assuming you have methods to get the game’s resources and setup
        String name = "Master";
        float startX = -100; // Starts off-screen
        float startY = 0;
        int maxHealth = 100;
        float speed = 1.0f; // Speed at which it moves into the screen

        masterEnemy = new MasterEnemy(name, startX, startY, maxHealth, speed);
    }


    private void initPaint() {
        progressBarPaint = new Paint();
        progressBarPaint.setColor(Color.RED);
        progressBarPaint.setStyle(Paint.Style.FILL);

        progressBarBackgroundPaint = new Paint();
        progressBarBackgroundPaint.setColor(Color.GRAY);
        progressBarBackgroundPaint.setStyle(Paint.Style.FILL);
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

    // Example collision check method
    boolean checkCollision(MasterEnemy masterEnemy) {
        // Assuming you have getBounds() methods for both player and masterEnemy
        return planeX < masterEnemy.x + enemyBitmap.getWidth() &&
                planeX + bulletBitmap.getWidth() > masterEnemy.x &&
                planeY < masterEnemy.y + enemyBitmap.getHeight() &&
                planeY + bulletBitmap.getHeight() > masterEnemy.y;
    }

    private void update() {
        if (gameOver) {
            return;
        }

        if(score > 300){
            createMaster();
        }

        updatePowerUps();
        spawnPowerUp();
        checkPowerUpCollision();

        // Update master enemy
        if (masterEnemy != null) {
            masterEnemy.update();

            // Check collision with player
            if (checkCollision(masterEnemy)) {
                // Handle game over logic
                endGame();
            }
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

    private void startReload() {
        isReloading = true;
        reloadStartTime = System.currentTimeMillis();
        // Optionally, you can show a reload message or animation
    }


    private void drawProgressBar(Canvas canvas, float progress) {
        int width = getWidth();
        int height = 20; // Height of the progress bar

        // Draw the background of the progress bar
        canvas.drawRect(0, getHeight() - height, width, getHeight(), progressBarBackgroundPaint);

        // Draw the progress
        canvas.drawRect(0, getHeight() - height, width * progress, getHeight(), progressBarPaint);
    }

    private void drawBackground(Canvas canvas) {
        int backgroundWidth = backgroundBitmap.getWidth();
        int backgroundHeight = backgroundBitmap.getHeight();

        // Draw the background three times
        canvas.drawBitmap(backgroundBitmap, backgroundX, 0, null);
        canvas.drawBitmap(backgroundBitmap, backgroundX + backgroundWidth, 0, null);
        canvas.drawBitmap(backgroundBitmap, backgroundX + 2 * backgroundWidth, 0, null);

        // If the background is larger than the screen, this ensures it repeats seamlessly
        if (backgroundX <= -backgroundWidth) {
            backgroundX = 0; // Reset background position
        }
    }

    private  void drawMaster(MasterEnemy masterEnemy, Canvas canvas){
        masterEnemy.draw(canvas);
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawRGB(0, 0, 0);

            if (isReloading) {
                long elapsedTime = System.currentTimeMillis() - reloadStartTime;
                float progress = (float) elapsedTime / RELOAD_TIME_MS;
                drawProgressBar(canvas, progress);
                if (elapsedTime >= RELOAD_TIME_MS) {
                    isReloading = false;
                    bulletsFired = 0; // Reset the bullet count
                }
            }

            // Draw background
            drawBackground(canvas);

            // Draw plane
            canvas.drawBitmap(planeBitmap, planeX, planeY, null);

            drawPowerUps(canvas);

            if (masterEnemy != null){
                drawMaster(masterEnemy,canvas);
            }

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
            canvas.drawText("Score: " + score, screenWidth - 350, 100, scorePaint);

            getHolder().unlockCanvasAndPost(canvas);

        }
    }

    public void fireBullet() {
        if (isReloading) return;

        if (bulletsFired >= MAX_BULLETS) {
            startReload();
        } else {
            synchronized (bullets) {
                if (bullets.size() < 100) {
                    float bulletX = planeX + planeBitmap.getWidth() / 2;// Example limit
                    long currentTime = System.currentTimeMillis();  // Initialize current time
                    if (currentTime - lastBulletTime > 100) { // Shoot a bullet every 100 milliseconds
                        bulletsFired++;
                        bullets.add(new Bullet(bulletX, planeY, 20, 20)); // Add horizontal speed
                        playBulletSound(); // Play sound on bullet release
                        lastBulletTime = currentTime; // Update last bullet time
                    }
                }
            }
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
                fireBullet();

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

    public class PowerUp {
        public float x, y;
        public Bitmap bitmap;
        public String type; // Different types of power-ups

        public PowerUp(Bitmap bitmap, float x, float y, String type) {
            this.bitmap = bitmap;
            this.x = x;
            this.y = y;
            this.type = type;
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
        // Inside Enemy class
        void reduceHealth(int damage) {
            currentHealth -= damage;
            if (currentHealth < 0) {
                currentHealth = 0;
            }

            // Update score based on enemy's health decrease
            if (currentHealth == 0) {
                score += 10; // Increase score by 10 when an enemy is destroyed
            }
        }


        // Check if enemy is dead
        boolean isDead() {
            return currentHealth <= 0;
        }
    }

    // MasterEnemy class extending Enemy
    class MasterEnemy extends Enemy {
        // Additional properties specific to MasterEnemy (if any)
        private float speed;

        MasterEnemy(String name, float x, float y, int maxHealth, float speed) {
            super(name, x, y, maxHealth);
            this.speed = speed;
            // MasterEnemy starts off-screen and should move in when needed
        }

        // Update method to control the movement and other behaviors
        @Override
        void reduceHealth(int damage) {
            currentHealth -= damage;
            if (currentHealth < 0) {
                currentHealth = 0;
            }

            // Update score based on enemy's health decrease
            if (currentHealth == 0) {
                score += 1000; // Increase score by 10 when an enemy is destroyed
            }
        }


        // Check if enemy is dead
        @Override
        boolean isDead() {
            return currentHealth <= 0;
        }

        void update() {
            // Move the MasterEnemy from left side to the screen
            if (x < 0) {
                x += speed;
            } else {
                // Keep MasterEnemy at a specific position when it has fully appeared
                x = 0;
            }
        }

        void draw(Canvas canvas) {
            // Draw other game elements...

            // Draw master enemy
            if (masterEnemy != null) {
                // Assuming you have a method to draw the master enemy
                canvas.drawBitmap(masterEnemyBitmap, masterEnemy.x, masterEnemy.y, null);
            }
        }

        // Optionally, you can override draw method if you have specific drawing logic
        // @Override
        // void draw(Canvas canvas) {
        //     // Draw MasterEnemy on the canvas
        // }
    }

    void endGame() {
        // Handle end game logic (e.g., show game over screen, stop game)
        gameOver = true;
    }

}