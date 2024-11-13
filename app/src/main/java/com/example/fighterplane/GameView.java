package com.example.fighterplane;

import com.example.fighterplane.components.*;
import static android.content.Context.MODE_PRIVATE;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
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

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameView extends SurfaceView implements Runnable, SensorEventListener {

    private Thread gameThread;
    private boolean isPlaying;
    private static final Random random = new Random();
    // Sensor variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private MediaPlayer mediaPlayer;
    private MediaPlayer lasermediaPlayer;
    private int poseidonHealth = 200000,dragonHealth = 100000;
    private boolean lowHealth;
    private boolean restart;
    public static int screenHeight;
    public static int screenWidth;

    private float enemySpawnRate = 0.05f;
    private int bulletInterval = 10;
    private static final long RELOAD_TIME_MS = 1000; // 1 seconds
    private float enemySpawnAcceleration = 1.2f;
    private static final int MAX_BULLETS = 100;
    private Bitmap  enemyBitmap,alienMonsterBitmap,alienMasterBitmap, alien1Bitmap,alien2Bitmap,alien3Bitmap, fireballBitmap,bulletBitmap,electricBulletBitmap,
                    powerUpBitmap,masterEnemyBitmap,replayButtonBitmap,swordFishBitmap,rocketBitmap,poseidonSwordBitmap,
                    healthBitmap,asteroidBitmap,shieldBitmap,dragonBitmap,pirahnagreenBitmap,crabBitmap,sharkBitmap,octopusgreenBitmap,
                    lightningBitmap,thunderBitmap,greenCoinBitmap,neonBitmap;
    public Bitmap planeBitmap,backgroundBitmap;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private long lastBulletTime;
    Timer timer = new Timer();
    private float backgroundX; // Position of the background
    private boolean WonDragon;
    private boolean enterPoseiden;
    private ValueAnimator beamAnimator;
    private int beamAlpha = 255; // Full opacity at start
    private boolean triplebullet;
    public static int score;// To keep track of points
    private SoundPool soundPool;
    private boolean isPaused;
    private boolean isLoaded;
    public String selectedMap = "space";
    private boolean Won;
    private int boomSoundId,youcallthatanattackSoundId,nurseSoundId,coinSoundId,poseidonSoundId,poseidonlaughSoundId,
                bulletSoundId,healthRechargeSoundId,bleepSoundId,triplepowerSoundId,doublepowerSoundId,
                DevilSoundId,laserSoundId,doomSoundId,squeakSoundId,painSoundId,casinoSoundId;
    private int BackgroundSoundId;
    private boolean doubleBullet;
    private boolean dragonSpawned;
    private boolean PoseidonSpawned;
    private boolean masterDead;
    private Enemy laserEnemy;
    private Bitmap pauseButtonBitmap;
    private int pauseButtonX = screenWidth - 150; // X position of the pause button
    private int pauseButtonY = screenHeight/2; // Y position of the pause button
    private float replayButtonX = screenWidth - 150;
    private float replayButtonY = screenHeight/2 - 200;
    private int pauseButtonWidth, pauseButtonHeight,enemySize;
    private int replayButtonWidth, replayButtonHeight;
    private int attack = 200,powerupSize;
    private float speed = 40f;
    public Hero hero;
    private int planeY,planeX,masterScore = 0;
    private int planeWidth = 70,planeHeight=70;
    private int bulletWidth = 5,bulletHeight=5;
    private List<PowerUp> powerUps;
    public static MasterEnemy masterEnemy;

    private int bulletsFired = 0;
    private boolean isReloading = false;
    private long reloadStartTime = 0;

    private float beamLength;
    private Paint progressBarPaint;
    private Paint progressBarBackgroundPaint;
    private ValueAnimator beamLengthAnimator;

    private float beamStartX;
    private float beamStartY;
    private float beamEndX;
    private float beamEndY;
    private float currentBeamLength;
    private final float maxBeamLength = 500; // Maximum length of the beam
    private final float beamGrowthRate = 0.5f; // Adjust this value for speed
    private boolean isBeamActive; // Controls if the beam should be active

    private float bitmapX; // X position of the bitmap
    private float bitmapY; // Y position of the bitmap
    private float waveOffset = 0; // Offset for waving effect
    private boolean movingRight; // Direction of movement
    private float speedneon = 20; // Speed of movement

    private Context context;

    private GameManager gameManager;

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        System.out.println("decoding resources");
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            options.inScaled = true;
        }
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        BitmapFactory.decodeResource(res, resId, options);

        // Log original dimensions
//        Log.d("BitmapDebug", "Original width: " + options.outWidth + ", height: " + options.outHeight);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Log sample size
//        Log.d("BitmapDebug", "Sample size: " + options.inSampleSize);

        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, out); // Compress to 50% quality
        // Log scaled dimensions
//        Log.d("BitmapDebug", "Scaled width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());

        return bitmap;
    }

    public void saveLevel(Context context, int level) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GameData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentLevel", level);
        editor.apply(); // or editor.commit() for synchronous saving
    }

    public int getLevel(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GameData", MODE_PRIVATE);
        // Default to level 1 if no level data is found
        return sharedPreferences.getInt("currentLevel", 1);
    }


    public void pause() {
        isPaused = true;   // Use this to skip update logic
        if (soundPool != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                soundPool.autoPause(); // Pause any playing sounds
            }
        }
        if (mediaPlayer != null) {
            mediaPlayer.pause(); // Pause any playing sounds
        }
        // Stop timers if you have any running
        if (timer != null) {
            timer.cancel(); // This stops the timer
        }
    }

    public void stopGame() {
        isPlaying = false;
    }

    public void pauseGame() {
        try {
            isPlaying = false;
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resumeGame() {
            isPlaying = true;
    }

    public void resume() {
        isPaused = false; // Resume game updates
        if (soundPool != null) {
            soundPool.autoResume(); // Resume paused sounds
        }

        if (mediaPlayer != null) {
            mediaPlayer.start(); // Pause any playing sounds
        }
        // Restart timers or other tasks if needed
    }


    public void stop() {
        // Additional cleanup, release resources here if needed
//        if (bitmap != null && !bitmap.isRecycled()) {
//            bitmap.recycle(); // Recycle bitmaps if needed
//        }
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Pause any playing sounds
        }
        // Free up other resources if necessary
    }

    // Save high score
    public void saveHighScore(int score) {
        SharedPreferences preferences = context.getSharedPreferences("game_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("high_score", score);
        editor.apply(); // Save the changes
    }

    // Retrieve high score
    public int getHighScore() {
        SharedPreferences preferences = context.getSharedPreferences("game_prefs", MODE_PRIVATE);
        return preferences.getInt("high_score", 0); // Default to 0 if no high score exists
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
        this.context = context;
        init(context);  // Initialize common properties
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);  // Initialize common properties
    }

    private void init(Context context) {
        // Initialize sensors
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register listener for sensor changes
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        // Your initialization code here
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        gameManager = new GameManager();

        this.screenHeight = getHeight();
        this.screenWidth = getWidth();

        bulletsFired = 0;
        doubleBullet = false;
        triplebullet = false;
        WonDragon = false;
        lowHealth = false;
        masterEnemy = null;
        enterPoseiden=false;
        selectedMap = "space";
        isPaused = false;
        doubleBullet = false;
        dragonSpawned = false;
        PoseidonSpawned = false;
        masterDead = false;
        bulletSoundId = soundPool.load(context, R.raw.squick, 2);
        boomSoundId = soundPool.load(context, R.raw.blast, 1);
        coinSoundId = soundPool.load(context, R.raw.powerup, 1);
        poseidonSoundId = soundPool.load(context, R.raw.master_dialogue, 1);
        bleepSoundId = soundPool.load(context, R.raw.bleep, 1);
        poseidonlaughSoundId = soundPool.load(context, R.raw.villian_laugh, 1);
        youcallthatanattackSoundId = soundPool.load(context, R.raw.you_call_that_an_attack, 1);
        healthRechargeSoundId = soundPool.load(context, R.raw.health_recharge, 1);
        nurseSoundId = soundPool.load(context, R.raw.do_you_want_me_to_call, 1);
        triplepowerSoundId = soundPool.load(context, R.raw.triple_power, 1);
        doublepowerSoundId = soundPool.load(context, R.raw.double_power, 1);
        DevilSoundId = soundPool.load(context, R.raw.devil_laugh, 1);
        laserSoundId = soundPool.load(context, R.raw.lasergun, 1);
        painSoundId = soundPool.load(context, R.raw.pain, 1);
        casinoSoundId = soundPool.load(context, R.raw.casino, 1);
        squeakSoundId = soundPool.load(context, R.raw.squeak, 1);
        doomSoundId = soundPool.load(context, R.raw.doom, 1);
        restart = false;
        isLoaded = false;
        isBeamActive = false;
        beamLength=0;
        enemySpawnRate = 0.1f;
        movingRight = true;
        enemySpawnAcceleration = 3;
        dragonSpawned = false;
        PoseidonSpawned = false;
        Won = false;
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> isLoaded = true);

        playBackgroundMusic(context);
        lastBulletTime = System.currentTimeMillis();
        doubleBullet = false;
        score = 0;
        enemySize = 40;
        powerupSize = 20;

        // Load bitmaps
        planeBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.plane, planeWidth, planeHeight);
        enemyBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.sword_fish, enemySize, enemySize);
        swordFishBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.sword_fish, enemySize, enemySize);
        bulletBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.bullet, bulletWidth, bulletHeight);
        fireballBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.bullet, bulletWidth, bulletHeight);
        lightningBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.lightningball, bulletWidth, bulletHeight);
        thunderBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.triple, powerupSize, powerupSize);
        rocketBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.rocket_launcher, bulletWidth, bulletHeight);
        healthBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.shield, powerupSize, powerupSize);
        asteroidBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.asteroid, powerupSize, powerupSize);
        greenCoinBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.greencoin, powerupSize, powerupSize);
        sharkBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.shark, enemySize, enemySize);
        shieldBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.health, powerupSize, powerupSize);
        dragonBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.dragon, 400, 400);
        pirahnagreenBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.pirahnagreen, enemySize, enemySize);
        crabBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.crab, enemySize, enemySize);
        octopusgreenBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.octopus, enemySize, enemySize);
        poseidonSwordBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.poseidon_sword, 200, 200);
        poseidonSwordBitmap = rotateBitmap(poseidonSwordBitmap, 270f);
        rocketBitmap = rotateBitmap(rocketBitmap, 90f);
        electricBulletBitmap = rotateBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.electricbullet, bulletWidth, bulletHeight),90f);
        backgroundBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.space, 10, 10); // Ensure this is 3 times the width of the screen
        // Load the power-up bitmap (adjust dimensions as needed)
        powerUpBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.powerup, powerupSize-8, powerupSize-8);
        masterEnemyBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.poseidon_left, 250, 250);
        neonBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.neon_red, 400, 400);
        alien1Bitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.alien1, 50, 50);
        alien2Bitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.alien2, 50, 50);
        alien3Bitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.alien3, 50, 50);
        alienMasterBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.alienmonster, 250, 250);
        alienMonsterBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.aliendragon, 300, 300);
        initializePauseButton();
        initializeReplayButton();

        backgroundX = 0; // Start background position at 0


        bullets = new ArrayList<>();
        enemies = new ArrayList<>();

        planeX = 0;
        planeY = 500;
        hero = new Hero("Plane",planeX,planeY,4000,planeBitmap);

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
        watchRestart(context);
    }

    void playNursedialogue(){
        soundPool.play(nurseSoundId, 1f, 1f, 1, 0, 1f);
    }

    void triplepowerdialogue(){
        soundPool.play(triplepowerSoundId, 1, 1, 1, 0, 1f);
    }

    void doublepowerdialogue(){
        soundPool.play(doublepowerSoundId, 1, 1, 1, 0, 1f);
    }

    void bleepSound(){
        soundPool.play(bleepSoundId, 1f, 1f, 1, 0, 1f);
    }

    void rechargeHealth(){
        soundPool.play(healthRechargeSoundId, 1, 1, 1, 0, 1f);
    }

    void playattackdialogue(){
        soundPool.play(youcallthatanattackSoundId, 1f, 1f, 1, 0, 1f);
    }

    void playsqueakSound(){
        if (selectedMap == "ocean"){
            soundPool.play(squeakSoundId, 0.3f, 0.3f, 2, 0, 1f);
        }else if (selectedMap == "space"){
            soundPool.play(doomSoundId, 0.3f, 0.3f, 2, 0, 1f);
        }
    }

    void playpainSound(){
        soundPool.play(painSoundId, 1, 1, 1, 0, 1f);
    }

    void playcasinoSound(){
        soundPool.play(casinoSoundId, 1, 1, 1, 0, 1f);
    }


    // Method to play the sound when a bullet is shot
    private void playBulletSound() {
        soundPool.play(bulletSoundId, 1f, 1f, 2, 0, 1f);
    }

    private void playLaserSound() {
        if (lasermediaPlayer == null) {
            lasermediaPlayer = MediaPlayer.create(context, R.raw.lasergun); // Ensure you place the file in res/raw
            lasermediaPlayer.setLooping(true); // Loop the background music
            lasermediaPlayer.setVolume(1f, 1f); // (leftVolume, rightVolume)
        }
        lasermediaPlayer.start();
    }


    private void stopLaserSound() {
        lasermediaPlayer.stop();
    }

    private void playcoinSound() {
        if (isLoaded) {
            soundPool.play(coinSoundId, 2f, 2f, 1, 0, 1f);
        }
    }

    private void DevilSound() {
        if (isLoaded) {
            soundPool.play(DevilSoundId, 1, 1, 1, 0, 1f);
        }
    }

    private void playBackgroundMusic(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.background); // Ensure you place the file in res/raw
            mediaPlayer.setLooping(true); // Loop the background music
            mediaPlayer.setVolume(0.6f, 0.6f); // (leftVolume, rightVolume)
        }
        mediaPlayer.start(); // Start playing the music
    }



    // Function to continuously watch the 'restart' variable
    public void watchRestart(Context context) {
        // Using a scheduled executor to check the 'restart' flag every second
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            if (restart) {
                pauseBackgroundMusic();
                init(context);    // Call the init function when restart is true
                restart = false; // Reset the restart flag after restarting
                gameManager.startGame();
            }
        }, 0, 1, TimeUnit.SECONDS); // Check every second
    }

    private void spawnPowerUp() {
        if (Math.random() < 0.1) {
            float powerUpX = screenWidth + powerUpBitmap.getWidth(); // Start off the right side
            float powerUpY = (float) Math.random() * (screenHeight - powerUpBitmap.getHeight());

            String[] types = {"fireball","lightning","rocket","doublebullet","triplebullet","health"}; // Different types of power-ups
            String randomType = types[(int) (Math.random() * types.length)];

            powerUps.add(new PowerUp(powerUpBitmap, powerUpX, powerUpY, "coin"));
            switch (randomType){
                case "fireball":
                    powerUps.add(new PowerUp(asteroidBitmap, powerUpX, powerUpY, randomType));
                    break;
                case "lightning":
                    powerUps.add(new PowerUp(lightningBitmap, powerUpX, powerUpY, randomType));
                    break;
                case "rocket":
                    powerUps.add(new PowerUp(greenCoinBitmap, powerUpX, powerUpY, randomType));
                    break;
                case "doublebullet":
                    powerUps.add(new PowerUp(shieldBitmap, powerUpX, powerUpY, randomType));
                    break;
                case "triplebullet":
                    powerUps.add(new PowerUp(thunderBitmap, powerUpX, powerUpY, randomType));
                    break;
                case "health":
                    powerUps.add(new PowerUp(healthBitmap, powerUpX, powerUpY, randomType));
                    break;
                default:
                    break;
            }
        }
    }

    public Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);  // Rotate by the specified angle

        // Create a new rotated bitmap
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void setSelectedShip(String ship) {
        hero.bitmap = planeBitmap;
    }

    public void startGame() {
        isPlaying = true;
    }

    public void createLaser(){
        laserEnemy = new Enemy("laser",getHeight()/2, getWidth()/2,1000000000,neonBitmap,200);
    }


    private void initializePauseButton() {
        if(!isPaused){
            pauseButtonBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.pausebutton,40,40); // Load pause button image
        }else{
            pauseButtonBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.resume,40,40); // Load pause button image
        }
        pauseButtonBitmap = rotateBitmap(pauseButtonBitmap, 90f);
        pauseButtonWidth = pauseButtonBitmap.getWidth();
        pauseButtonHeight = pauseButtonBitmap.getHeight();
    }

    private void initializeReplayButton() {
        // Load the bitmap for the replay button
        replayButtonBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.replay_button,40,40);
        replayButtonBitmap = rotateBitmap(replayButtonBitmap, 90f);
        replayButtonWidth = replayButtonBitmap.getWidth();
        replayButtonHeight = replayButtonBitmap.getHeight();
    }

    private void drawReplayButton(Canvas canvas) {
        // Draw the replay button in the center of the screen
        canvas.drawBitmap(replayButtonBitmap, screenWidth - 150, screenHeight/2, null);
    }

    private void drawPauseButton(Canvas canvas) {
        canvas.drawBitmap(pauseButtonBitmap, screenWidth - 150, screenHeight/2 - 200, null);
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
//            if (hero.x < powerUp.x + powerUp.bitmap.getWidth() &&
//                    hero.x + hero.bitmap.getWidth() > powerUp.x &&
//                    hero.y < powerUp.y + powerUp.bitmap.getHeight() &&
//                    hero.y + hero.bitmap.getHeight() > powerUp.y)
            if (pixelPerfectCollision(hero.bitmap, hero.x, hero.y, powerUp.bitmap, powerUp.x, powerUp.y))
            {
                collectedPowerUps.add(powerUp); // Collect the power-up
                applyPowerUpEffect(powerUp);
            }
        }

        powerUps.removeAll(collectedPowerUps); // Remove collected power-ups
    }

    private void applyPowerUpEffect(PowerUp powerUp) {
        score += 100;
        String[] types = {"fireball","coin","lightning","rocket","doublebullet","triplebullet","health"}; // Different types of power-ups
        switch (powerUp.type) {
            case "lightning":
                bulletBitmap = electricBulletBitmap;
                doubleBullet = false;
                triplebullet = false;
                break;
            case "fireball":
//                playpainSound();
                hero.currentHealth -= 50;
                bulletBitmap = fireballBitmap;
                doubleBullet = false;
                triplebullet = false;
                break;
            case "triplebullet":
                triplebullet = true;
                triplepowerdialogue();
                break;

                case "coin":
                    playcoinSound();
                    score += 500;
                    break;

            case "rocket":
                playcasinoSound();
                bulletBitmap = rocketBitmap;
                doubleBullet = false;
                triplebullet = false;
                break;
            case "doublebullet":
                doublepowerdialogue();
                doubleBullet = true;
                break;
            case "health":
                // Increase the shooting speed temporarily
                hero.currentHealth = hero.maxHealth;
                rechargeHealth();
                break;
        }
    }

    private void drawPowerUps(Canvas canvas) {
        for (PowerUp powerUp : powerUps) {
            canvas.drawBitmap(powerUp.bitmap, powerUp.x, powerUp.y, null);
        }
    }

    void createMaster(String mastername){
        // Assuming you have methods to get the game’s resources and setup
        String name = mastername;
        float startX = getWidth(); // Starts off-screen
        float startY = getHeight() / 2 - masterEnemyBitmap.getHeight()/2;
        int poseidonmaxHealth = poseidonHealth;
        int dragonmaxHealth = dragonHealth;
        float speed = 5.0f; // Speed at which it moves into the screen

        if (name == "poseidon"){
            System.out.println("Poseiden is back🔥🔥🔥🔥🔥");
            masterEnemy = new MasterEnemy(name, startX, startY, poseidonmaxHealth, speed, masterEnemyBitmap,10000);
        }else if (name == "dragon"){
            masterEnemy = new MasterEnemy(name, startX, startY - 200, dragonmaxHealth, speed, dragonBitmap,10000);
        } else if (name == "alienMaster") {
            masterEnemy = new MasterEnemy(name, startX, startY, poseidonmaxHealth, speed, alienMasterBitmap,10000);
        } else if (name == "alienMonster") {
            masterEnemy = new MasterEnemy(name, startX, startY, dragonmaxHealth, speed, alienMonsterBitmap,10000);
        }
    }

    void recycleBitmaps(){
        // Remove bullets and enemies safely
        synchronized (bullets) { // Synchronize access to bullets list
            bullets.clear();
            for (Bullet bullet:bullets) {
                if (bullet.bitmap != null && !bullet.bitmap.isRecycled()){
                    bullet.bitmap.recycle();
                    bullet.bitmap = null;
                }
            }
        }
        synchronized (enemies) { // Synchronize access to enemies list
            enemies.clear();
            for (Enemy enemy : enemies) {
                if (enemy.bitmap != null && !enemy.bitmap.isRecycled()) {
                    enemy.bitmap.recycle(); // Safely recycle the bitmap
                    enemy.bitmap = null; // Set bitmap to null to avoid accidental usage
                }
            }
        }
        System.gc();

    }


    private void initPaint() {
        progressBarPaint = new Paint();
        progressBarPaint.setColor(Color.RED);
        progressBarPaint.setStyle(Paint.Style.FILL);

        progressBarBackgroundPaint = new Paint();
        progressBarBackgroundPaint.setColor(Color.YELLOW);
        progressBarBackgroundPaint.setStyle(Paint.Style.FILL);
    }


    @Override
    public void run() {
        while (isPlaying) {
            if (!gameManager.isGameOver()) {
                update();
            }
            draw();
            System.out.println(getWidth() + "is the width " + getHeight() + "is the height");
            try {
                Thread.sleep(16); // approx 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean pixelPerfectCollision(Bitmap bitmapA, float xA, float yA, Bitmap bitmapB, float xB, float yB) {
        if ((bitmapA != null && !bitmapA.isRecycled()) && (bitmapB != null && !bitmapB.isRecycled())) {
            // Get the width and height of the bitmaps
            int widthA = bitmapA.getWidth();
            int heightA = bitmapA.getHeight();
            int widthB = bitmapB.getWidth();
            int heightB = bitmapB.getHeight();

            // Calculate the overlapping rectangle
            int xOverlap = Math.max((int) xA, (int) xB);
            int yOverlap = Math.max((int) yA, (int) yB);
            int xOverlapEnd = Math.min((int) (xA + widthA), (int) (xB + widthB));
            int yOverlapEnd = Math.min((int) (yA + heightA), (int) (yB + heightB));

            // No overlap
            if (xOverlap >= xOverlapEnd || yOverlap >= yOverlapEnd) {
                return false;
            }

            // Iterate through the pixels in the overlapping rectangle
            for (int x = xOverlap; x < xOverlapEnd; x++) {
                for (int y = yOverlap; y < yOverlapEnd; y++) {
                    // Get the alpha component of both pixels
                    int alphaA = Color.alpha(bitmapA.getPixel(x - (int) xA, y - (int) yA));
                    int alphaB = Color.alpha(bitmapB.getPixel(x - (int) xB, y - (int) yB));

                    // If both pixels have non-zero alpha, there's a collision
                    if (alphaA > 0 && alphaB > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    void createEnemies(){
        // Spawn new enemies
        if (Math.random() < enemySpawnRate) {
            String[] types = new String[0];
            float enemyX = screenWidth + enemySize; // Start off the right side
            float enemyY = (float) Math.random() * (screenHeight - enemySize);
            if (selectedMap == "space"){
                types = new String[]{"alien1", "alien2", "alien3"};
            }else{
                types = new String[]{"crab", "octopus", "shark"};
            }
            String randomType = types[(int) (Math.random() * types.length)];


            if (masterEnemy != null && masterEnemy.name == "poseidon"){
                double probability = random.nextDouble();
                if (probability <= 0.09) {
                    enemies.add(new Enemy("sword", enemyX, enemyY, 5500, poseidonSwordBitmap, 10000)); // Example enemy with 100 health
                    return;
                }
            }

            switch (randomType){
                case "crab":
                    enemies.add(new Enemy("Enemy", enemyX, enemyY, 500, crabBitmap, 50)); // Example enemy with 100 health
                    break;

                case "octopus":
                    enemies.add(new Enemy("Enemy", enemyX, enemyY, 500, octopusgreenBitmap, 100)); // Example enemy with 100 health
                    break;

                case "shark":
                    enemies.add(new Enemy("Enemy", enemyX, enemyY, 500, sharkBitmap, 200)); // Example enemy with 100 health
                    break;

                case "alien1":
                    enemies.add(new Enemy("Enemy", enemyX, enemyY, 500, alien1Bitmap, 200)); // Example enemy with 100 health
                    break;

                case "alien2":
                    enemies.add(new Enemy("Enemy", enemyX, enemyY, 500, alien2Bitmap, 200)); // Example enemy with 100 health
                    break;
                case "alien3":
                    enemies.add(new Enemy("Enemy", enemyX, enemyY, 500, alien3Bitmap, 200)); // Example enemy with 100 health
                    break;

                default:
                    break;
            }
        }
    }

    void updateBackground(){
        // Update background position
        backgroundX -= 2; // Adjust this value to control the speed of the scrolling
        if (backgroundX <= -backgroundBitmap.getWidth()) {
            backgroundX = 0; // Reset background position when it moves off-screen
        }
    }

    public void displayWinMessage(Canvas canvas, String message) {
        // Set up the paint for drawing text
        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Text color
        paint.setTextSize(100);      // Text size
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Bold font

        // Get screen width and height (assuming canvas size matches screen)
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        // Draw the text "You WonDragon the Game" in the center of the screen
        canvas.drawText(message, canvasWidth / 2, canvasHeight / 2, paint);
    }

    public void displayLevelUpMessage(Canvas canvas, String message) {
        // Set up the paint for drawing text
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW); // Text color
        paint.setTextSize(100);      // Text size
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Bold font

        // Get screen width and height (assuming canvas size matches screen)
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        // Draw the text "You WonDragon the Game" in the center of the screen
        canvas.drawText(message, canvasWidth / 2, canvasHeight / 2 + 100, paint);
    }

    public void setSelectedMap(String map) {
        this.selectedMap = map; // Store the selected map for game logic
    }



    private final Object bulletLock = new Object();
    private final Object enemyLock = new Object();

    private void update() {

        if (gameManager.isGameOver() || Won) {
            return;
        }

        if (!isPaused){

            initializePauseButton();
        }else if(isPaused){
            initializePauseButton();
        }

        if (isPaused) {
            return; // Skip the update logic if the game is paused
        }

        if (WonDragon && enterPoseiden){
            enemySpawnRate *= enemySpawnAcceleration;
            isPaused = true;
            soundPool.play(poseidonlaughSoundId, 1, 1, 1, 0, 1f);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    soundPool.play(poseidonSoundId, 1, 1, 1, 0, 1f);
                    // Code to execute after 3 seconds
                }
            }, 4000);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Code to execute after 3 seconds
                    isPaused = false;
                }
            }, 8000); // Delay in milliseconds (3000ms = 3 seconds)
        }

        if (enterPoseiden) {
            System.out.println("🔥🔥🔥🔥🔥🔥💀💀💀💀💀💀💀");
        }

        if (score > masterScore && !WonDragon && !dragonSpawned) {
            dragonSpawned = true;
            DevilSound();
            System.out.println("Creating master");
            if (selectedMap == "ocean"){
                createMaster("dragon");
            }else if(selectedMap == "space"){
                createMaster("alienMonster");
            }
        }

        if ( WonDragon && !PoseidonSpawned && enterPoseiden) {
            enterPoseiden = false;
            PoseidonSpawned = true;
            if (selectedMap == "ocean"){
                createMaster("poseidon");
            }else if(selectedMap == "space"){
                createMaster("alienMaster");
            }
            System.out.println("Creating poseidon");
        }

        // Update master enemy
        if (masterEnemy != null) {
            System.out.println("master's position: " + masterEnemy.x);
            float maxPoint;
            if (masterEnemy.name == "poseidon" || masterEnemy.name == "alienMaster" ){
                maxPoint = getWidth() - masterEnemy.bitmap.getWidth();
            }else{
                maxPoint = getWidth() - masterEnemy.bitmap.getWidth();
            }
            if (masterEnemy.x > maxPoint){
                masterEnemy.x -= 10;
            } else if ((masterEnemy.name == "dragon" || masterEnemy.name == "alienMonster") && !isBeamActive) {
                isBeamActive = true;
                createLaser();
                playLaserSound();
            }

            if (pixelPerfectCollision(hero.bitmap, hero.x, hero.y, masterEnemy.bitmap, masterEnemy.x, masterEnemy.y)) {
                hero.currentHealth -= masterEnemy.attack;

//                playpainSound();
                if (hero.isDead()){
                    endGame();
                }
            }
        }

        if (isBeamActive && pixelPerfectCollision(hero.bitmap, hero.x, hero.y, laserEnemy.bitmap, laserEnemy.x, laserEnemy.y)) {
            hero.currentHealth -= laserEnemy.attack;
            playpainSound();
            if (hero.isDead()){
                endGame();
            }
        }

        // Temporary lists to hold bullets and enemies to remove
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();

        // Update bullets
        synchronized (bullets) { // Synchronize access to bullets list
            for (Bullet bullet : bullets) {
                bullet.x += bullet.speedX;
                if (bullet.name == "bullet-left"){
                    bullet.y -= bullet.speedX;
                } else if (bullet.name == "bullet-right") {
                    bullet.y += bullet.speedX;
                }
                if (bullet.x > screenWidth) {
                    bulletsToRemove.add(bullet); // Mark for removal
                }
            }
        }

        // Update enemies
        synchronized (enemies) { // Synchronize access to enemies list
            for (Enemy enemy : enemies) {
                enemy.x -= 50; // Move the enemy to the left
                if (enemy.name == "sword"){
                    enemy.x -= 80;
                }
                if (enemy.x + enemyBitmap.getWidth() < 0) {
                    enemiesToRemove.add(enemy); // Mark for removal
                }
            }
        }

        // Check for collisions
        synchronized (bullets) { // Synchronize access to bullets list
            synchronized (enemies) { // Synchronize access to enemies list
                for (Bullet bullet : bullets) {
                    for (Enemy enemy : enemies) {
                        if (pixelPerfectCollision(bulletBitmap, bullet.x, bullet.y, enemyBitmap, enemy.x, enemy.y)) {
                            System.out.println("attacking enemy");
                            enemy.reduceHealth(bullet.attack);
                            System.out.println(enemy.currentHealth + "is the enemy's current health");
                            bulletsToRemove.add(bullet); // Mark for removal
                            if (enemy.isDead()) {
                                playsqueakSound();
                                enemiesToRemove.add(enemy); // Mark for removal
                            }
                        }
                    }

                    if ( masterEnemy != null && pixelPerfectCollision(bulletBitmap, bullet.x, bullet.y, masterEnemyBitmap, masterEnemy.x, masterEnemy.y)) {
                        System.out.println("attacking master");
                        masterEnemy.reduceHealth(bullet.attack);
                        System.out.println(masterEnemy.currentHealth + "is the master's current health");
                        bulletsToRemove.add(bullet); // Mark for removal

                        if (masterEnemy != null && masterEnemy.isDead() && (masterEnemy.name == "dragon" || masterEnemy.name == "alienMonster")) {
                            WonDragon = true;
                            masterEnemy.startDying(context); // Trigger the dying animation
                            masterEnemy = null; // Mark for removal
                            enterPoseiden = true;
                            System.out.println("Dragon died🥲");
                            isBeamActive = false;
                            laserEnemy = null;
                            if (lasermediaPlayer != null){
                                stopLaserSound();
                            }
                        }
                        if (masterEnemy != null && masterEnemy.isDead() && (masterEnemy.name == "poseidon" || masterEnemy.name == "alienMaster")) {
                            if (masterEnemy.name == "poseidon"){
                                saveLevel(context, 1);
                            }else{
                                saveLevel(context, 2);
                            }
                            pauseBackgroundMusic();
                            masterEnemy = null; // Mark for removal
                            System.out.println("master died🥲");
                            Won = true;
                        }
                    }
                }
            }
        }

        // In the main game loop
        if (masterEnemy != null) {
            if (masterEnemy.isDying) {
                masterEnemy.updateDyingPosition(); // Move down the screen
                if (masterEnemy.dyingPositionY > screenHeight) {
                    masterEnemy = null; // Remove the master enemy when off-screen
                }
            }
        }

        // Remove bullets and enemies safely
        synchronized (bullets) { // Synchronize access to bullets list
            bullets.removeAll(bulletsToRemove);
        }
        synchronized (enemies) { // Synchronize access to enemies list
            enemies.removeAll(enemiesToRemove);
        }

        // Check for collision between plane and enemies
            for (Enemy enemy : enemies) {
                if (pixelPerfectCollision(hero.bitmap, hero.x, hero.y, enemyBitmap, enemy.x, enemy.y)) {
                    hero.currentHealth -= enemy.attack;
                    playpainSound();
                    if (hero.isDead()){
                        endGame();
                    }
                    break;
                }
            }

        createEnemies();
        updatePowerUps();
        spawnPowerUp();
        checkPowerUpCollision();
        // Call background updates if necessary
        if (masterEnemy == null){
            updateBackground();
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
        canvas.drawRect(100, getHeight() - height, width, getHeight(), progressBarBackgroundPaint);

        // Draw the progress
        canvas.drawRect(100, getHeight() - height, width * progress, getHeight(), progressBarPaint);
    }

    private void drawBackground(Canvas canvas) {
        int backgroundWidth = backgroundBitmap.getWidth();
        int backgroundHeight = backgroundBitmap.getHeight();

        // Draw the background three times
        canvas.drawBitmap(backgroundBitmap, backgroundX, -950, null);
        canvas.drawBitmap(backgroundBitmap, 2*backgroundX, -950, null);
        canvas.drawBitmap(backgroundBitmap, 3*backgroundX, -950, null);
        canvas.drawBitmap(backgroundBitmap, 4*backgroundX, -950, null);
        canvas.drawBitmap(backgroundBitmap, 5*backgroundX, -950, null);
        canvas.drawBitmap(backgroundBitmap, 6*backgroundX, -950, null);
        canvas.drawBitmap(backgroundBitmap, 7*backgroundX, -950, null);
        canvas.drawBitmap(backgroundBitmap, 8*backgroundX, -950, null);

        // If the background is larger than the screen, this ensures it repeats seamlessly
        if (backgroundX < -backgroundBitmap.getWidth()) {
            backgroundX = 0; // Reset background position
        }
    }

    private void drawPauseOverlay(Canvas canvas) {
        if (isPaused) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(100);
            canvas.drawText("Paused", screenWidth / 2 - 150, screenHeight / 2, paint);
        }
    }

    void drawHealthBar(Canvas canvas,Enemy enemy){

//        canvas.drawBitmap(enemy.bitmap, enemy.x, enemy.y, null);

        // Draw health bar
        Paint healthPaint = new Paint();
        healthPaint.setColor(Color.RED); // Full health color (background)
        float healthBarWidth = 60;
        float healthBarHeight = 10;
        if (enemy.name == "poseidon" || enemy.name == "dragon" || enemy.name == "alienMaster" || enemy.name == "alienMonster"){
            healthPaint.setColor(Color.YELLOW); // Full health color (background)
            healthBarWidth = 500;
            healthBarHeight = 30;
        }
        float healthPercentage = (float) enemy.currentHealth / enemy.maxHealth;

        // Background (full health)
        canvas.drawRect(enemy.x , enemy.y - 20, enemy.x + healthBarWidth, enemy.y - 20 + healthBarHeight, healthPaint);

        // Foreground (current health)
        healthPaint.setColor(Color.GREEN); // Remaining health color
        canvas.drawRect(enemy.x , enemy.y - 20, enemy.x + healthBarWidth * healthPercentage, enemy.y - 20 + healthBarHeight, healthPaint);
    }

    void drawHealthBarHero(Canvas canvas, Hero hero) {
        Paint healthPaint = new Paint();
        Paint borderPaint = new Paint();

        // Health bar dimensions
        float healthBarWidth = 500;
        float healthBarHeight = 40;
        float healthPercentage = (float) hero.currentHealth / hero.maxHealth;

        if (healthPercentage == 0.2f && !lowHealth){
            playNursedialogue();
            lowHealth =true;
        }

        // Coordinates for the health bar (adjust as needed)
        float left = getWidth() - healthBarWidth - 100;
        float top = 10;
        float right = getWidth() - 100;
        float bottom = top + healthBarHeight;
        float cornerRadius = 20;

        // Shadow effect behind the health bar
        healthPaint.setColor(Color.GRAY);
        healthPaint.setShadowLayer(10, 5, 5, Color.BLACK);
        canvas.drawRoundRect(left + 5, top + 5, right + 5, bottom + 5, cornerRadius, cornerRadius, healthPaint);

        // Background (full health) - dark gray color
        healthPaint.setColor(Color.DKGRAY);
        healthPaint.clearShadowLayer();
        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, healthPaint);

        // Foreground (current health) with gradient fill
        LinearGradient gradient = new LinearGradient(
                left, top,
                left + healthBarWidth * healthPercentage, top,
                Color.BLUE, Color.BLUE*2,
                Shader.TileMode.CLAMP
        );
        healthPaint.setShader(gradient);
        canvas.drawRoundRect(left, top, left + healthBarWidth * healthPercentage, bottom, cornerRadius, cornerRadius, healthPaint);

        // Draw a border around the health bar
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5);
        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, borderPaint);
    }

    private  void drawMaster(MasterEnemy masterEnemy, Canvas canvas){
        masterEnemy.draw(canvas);
    }

    private void drawMovingNeon(Canvas canvas) {
        // Determine the width of the canvas and the bitmap
        Paint paint = new Paint();
        System.out.println("🎨🎨🎨🎨🎨🎨🎨🎨🎨🎨🎨🎨🎨");
        paint.setAntiAlias(true); // Smooth edges for the neon effect
        paint.setStyle(Paint.Style.FILL); // Fills shapes rather than just outlines
        beamStartX = getWidth() - (float)masterEnemy.bitmap.getWidth() /2 - 250;
        beamEndX = 0;
        beamStartY = (float) getHeight() /2 + 80;
        beamEndY = (float) getHeight() /2 + 100;

        // Update the position of the bitmap for the waving effect
        bitmapX += (movingRight ? speedneon : -speedneon); // Move right if movingRight is true, else move left

        // Check if the bitmap has reached the left or right edge
        if (bitmapX >= 0) {
            movingRight = false; // Change direction to left when hitting the right edge
        } else if (bitmapX <= -500) {
            movingRight = true; // Change direction to right when hitting the left edge
        }

        // Apply wave effect to the bitmap's Y position
        bitmapY = (float) (150 + 20 * Math.sin(waveOffset)); // Adjust Y position for wave effect
        waveOffset += 0.3f; // Update wave offset for continuous wave motion

        // Draw the bitmap
        canvas.drawBitmap(laserEnemy.bitmap, laserEnemy.x, laserEnemy.y, paint);
    }

    private void drawRoundedRect(Canvas canvas, float left, float top, float right, float bottom, float radius, Paint paint) {
        Path path = new Path();
        path.addRoundRect(left, top, right, bottom, radius, radius, Path.Direction.CW);
        canvas.drawPath(path, paint);
    }

    public void displayScores(Canvas canvas, int currentScore) {
        // Create a Paint object for the text
        Paint scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(50);

        // Load the wood texture bitmap
        Bitmap woodTexture = BitmapFactory.decodeResource(getResources(), R.drawable.wood_texture); // Make sure you have this texture in your drawable resources

        // Create a Paint object for the background box with texture
        Paint backgroundPaint = new Paint();
        backgroundPaint.setShader(new BitmapShader(woodTexture, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        // Background dimensions
        int boxWidth = 500;
        int boxHeight = 120; // Enough height for both scores
        int boxX = screenWidth - boxWidth - 80; // 80 pixels from the right
        int boxY = 70; // Top margin
        float cornerRadius = 30f; // Radius for rounded corners

        // Draw background box with rounded corners
        drawRoundedRect(canvas, boxX, boxY, boxX + boxWidth, boxY + boxHeight, cornerRadius, backgroundPaint);

        // Retrieve high score from SharedPreferences
        int highScore = getHighScore();

        // Update high score if current score is greater
        if (currentScore > highScore) {
            highScore = currentScore; // Update high score to current score
            saveHighScore(highScore); // Save new high score
        }

        // Draw current score
        canvas.drawText("Score: " + currentScore, boxX + 20, boxY + 50, scorePaint); // 20 pixels from left

        // Draw high score
        canvas.drawText("High Score: " + highScore, boxX + 20, boxY + 100, scorePaint); // 50 pixels from the top
    }




    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawRGB(0, 0, 0);

            // Draw background
            drawBackground(canvas);

            if (isReloading) {
                long elapsedTime = System.currentTimeMillis() - reloadStartTime;
                float progress = (float) elapsedTime / RELOAD_TIME_MS;
                drawProgressBar(canvas, progress);
                if (elapsedTime >= RELOAD_TIME_MS) {
                    isReloading = false;
                    bulletsFired = 0; // Reset the bullet count
                }
            }

            // Draw plane
            canvas.drawBitmap(hero.bitmap, hero.x, hero.y, null);

            drawPowerUps(canvas);

            if (masterEnemy != null){
                enemyBitmap = masterEnemy.bitmap;
            }

            // Draw bullets
            synchronized (bullets) {
                for (Bullet bullet : bullets) {
                    if (bullet.bitmap != null && !bullet.bitmap.isRecycled()) {
                        canvas.drawBitmap(bullet.bitmap, bullet.x, bullet.y, null);
                    }
                }
            }

            if (isBeamActive && !isPaused) {
                drawMovingNeon(canvas);
            }

            if (masterEnemy != null) {
                masterEnemy.draw(canvas);
            }

            // Draw enemies
            synchronized (enemies) {
                for (Enemy enemy : enemies) {
                    if (enemy.bitmap != null && !enemy.bitmap.isRecycled()) {
                        canvas.drawBitmap(enemy.bitmap, enemy.x, enemy.y, null);
                    }
                }
            }

            // Draw enemies and their health bars
            for (Enemy enemy : enemies) {
                drawHealthBar(canvas,enemy);
            }

            if (masterEnemy != null){
                drawHealthBar(canvas, masterEnemy);
            }

            if (hero != null){
                drawHealthBarHero(canvas, hero);
            }

            // Draw score at the top-right corner
            displayScores(canvas, score);

            // Draw the pause button on top
            drawPauseButton(canvas);
            drawReplayButton(canvas);

            if (gameManager.isGameOver()){
                displayWinMessage(canvas, "Game Over");
            }

            if (Won){
                displayWinMessage(canvas, "You Won");
                System.out.println("We won 🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥");
                if (getLevel(context) == 1){
                    displayLevelUpMessage(canvas, "Level 1 Unlocked!");
                }
            }

            // Unlock and post the canvas
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void fireBullet() {
        if (isReloading) return;

        if (bulletsFired >= MAX_BULLETS) {
            startReload();
            bleepSound();
        } else {
            synchronized (bullets) {
                if (bullets.size() < 200) {
                    float bulletX = hero.x + hero.bitmap.getWidth() / 2;// Example limit
                    long currentTime = System.currentTimeMillis();  // Initialize current time
                    if (currentTime - lastBulletTime > bulletInterval) { // Shoot a bullet every 100 milliseconds
                        bulletsFired++;
                        if (doubleBullet){
                            bullets.add(new Bullet("bullet",bulletX, hero.y + hero.bitmap.getWidth()/1.5f, attack, speed, bulletBitmap)); // Add horizontal speed
                            bullets.add(new Bullet("bullet",bulletX, hero.y + hero.bitmap.getWidth()/3, attack, speed, bulletBitmap)); // Add horizontal speed
                        }else if(triplebullet){
                            bullets.add(new Bullet("bullet-left",bulletX, hero.y + hero.bitmap.getWidth()/3, attack, speed, bulletBitmap)); // Add horizontal speed
                            bullets.add(new Bullet("bullet",bulletX, hero.y + hero.bitmap.getWidth()/2.5f, attack, speed, bulletBitmap)); // Add horizontal speed
                            bullets.add(new Bullet("bullet-right",bulletX, hero.y + hero.bitmap.getWidth()/2, attack, speed, bulletBitmap)); // Add horizontal speed
                        }else{
                            bullets.add(new Bullet("bullet",bulletX, hero.y + hero.bitmap.getWidth()/2, attack, speed, bulletBitmap)); // Add horizontal
                        }
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
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (touchX >= screenWidth - 150 && touchX <= (screenWidth - 150 + pauseButtonWidth) &&
                        touchY >= screenHeight/2 - 200 && touchY <= (screenHeight/2 - 200 + pauseButtonHeight)) {

                    isPaused = !isPaused; // Toggle pause state
                    initializePauseButton();
                }

                // Check if the touch is within the bounds of the replay button
                if (touchX >= screenWidth - 150 && touchX <= screenWidth - 150 + replayButtonWidth
                        && touchY >= screenHeight/2 && touchY <= screenHeight/2 + replayButtonHeight) {
                    // Replay button clicked
                    restartGame();
                    return true; // Return true to consume the touch event
                }

            case MotionEvent.ACTION_MOVE:
                // Shoot bullets while dragging
                if (isLoaded && !isPaused && !gameManager.isGameOver()) {
                    fireBullet();
                }

                break;

            case MotionEvent.ACTION_UP:
                // No additional shooting when the touch is lifted
                break;
        }

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !isPaused && !gameManager.isGameOver() && !Won) {
            float yAxisValue = event.values[0]; // Y-axis accelerometer value
            float XAxisValue = event.values[1]; // Y-axis accelerometer value

            // Set a scaling factor to control sensitivity (adjust as needed)
            float ysensitivity = 6.0f;
            float xsensitivity = 0.2f;

            // Update plane's Y position based on the tilt (inversing because tilt is opposite)
            hero.y -= yAxisValue * ysensitivity;
            hero.x -= XAxisValue * xsensitivity;

            // Prevent plane from moving off the screen
            if (hero.y < (float) -hero.bitmap.getHeight() /2) {
                hero.y = (float) -hero.bitmap.getHeight() /2; // Prevent going above the screen
            } else if (hero.y > (float) -hero.bitmap.getHeight()/2 + (float) getHeight()) {
                hero.y = (float) -hero.bitmap.getHeight()/2 + (float) getHeight(); // Prevent going below the screen
            } else if(hero.x < 0){
                hero.x = 0;
            }

            // Redraw the game view to reflect the updated position
            invalidate();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Unregister the sensor listener when not in use
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sensorManager.unregisterListener(this);
    }

    void pauseBackgroundMusic(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if ( lasermediaPlayer != null) {
            lasermediaPlayer.stop();
            lasermediaPlayer.release();
            lasermediaPlayer = null;
        }
    }

    // Inside your GameView class
    public void restartGame() {
        // Create an Intent to start MainActivity
        Intent intent = new Intent(getContext(), MainActivity.class);

        // Optional: Add flags to clear the current activity from the stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Start MainActivity
        getContext().startActivity(intent);

        // Finish the current activity
        ((Activity) getContext()).finish();
        recycleBitmaps();
        pauseBackgroundMusic();
        System.gc();
    }



    void endGame() {
        // Handle end game logic (e.g., show game over screen, stop game)
        gameManager.endTheGame();
        pauseBackgroundMusic();
        recycleBitmaps();
        soundPool.play(boomSoundId, 1, 1, 1, 0, 1f);
    }
}