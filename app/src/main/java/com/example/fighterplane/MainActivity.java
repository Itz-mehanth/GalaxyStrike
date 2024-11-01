package com.example.fighterplane;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout startLayout, storeLayout;
    private Button startButton, storeButton,backButton;
    private TextView coinBalanceText, highScoreText;
    private String selectedShip;

    private int currentCoins = 0;
    private int highScore = 0;
    private GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize layouts and buttons
        startLayout = findViewById(R.id.start_layout);
        storeLayout = findViewById(R.id.store_layout);
        startButton = findViewById(R.id.startButton);
        storeButton = findViewById(R.id.storeButton);
        coinBalanceText = findViewById(R.id.coinBalanceText);
        highScoreText = findViewById(R.id.highScoreText);
        backButton = findViewById(R.id.back_to_home);



        gameView = new GameView(this);
        gameView.setVisibility(View.GONE);
        addContentView(gameView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        // Hide the Action Bar and System UI
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        hideSystemUI();

        // Load player data
        loadPlayerData();

        // Set UI elements
        updateUI();

        // Button listeners
        backButton.setOnClickListener(view -> onBackToHomeClicked());
        startButton.setOnClickListener(v -> startGame());
        storeButton.setOnClickListener(v -> openStore());
    }

    private void startGame() {
        startLayout.setVisibility(View.GONE);
        storeLayout.setVisibility(View.GONE);
        gameView.setVisibility(View.VISIBLE);

        // Pass selectedShip to GameView
        gameView.setSelectedShip(selectedShip);
        gameView.startGame();
        Toast.makeText(this, "Starting game with " + selectedShip, Toast.LENGTH_SHORT).show();
    }


    private void onBackToHomeClicked() {
        // Set the visibility of layouts to show the home screen
        startLayout.setVisibility(View.VISIBLE);
        storeLayout.setVisibility(View.GONE);
    }


    private void updateUI() {
        highScoreText.setText("High Score: " + highScore);
    }

    private void openStore() {
        startLayout.setVisibility(View.GONE);
        storeLayout.setVisibility(View.VISIBLE);
        populateShipStore();
    }

    private void populateShipStore() {
        LinearLayout shipContainer = findViewById(R.id.shipContainer);
        shipContainer.removeAllViews(); // Clear existing views

        // Create example ships
        List<Ship> ships = new ArrayList<>();
        ships.add(new Ship("Speed Ship", 10000));
        ships.add(new Ship("Shield Ship", 20000));
        ships.add(new Ship("Power Ship", 30000));
        ships.add(new Ship("Stealth Ship", 40000));

        // Add each ship to the store
        for (Ship ship : ships) {
            addShipToStore(ship, shipContainer);
        }
    }

    private void addShipToStore(Ship ship, LinearLayout shipContainer) {
        // Inflate the item_ship layout
        View shipView = getLayoutInflater().inflate(R.layout.item_ship, shipContainer, false);

        // Find views in the inflated layout
        TextView shipNameTextView = shipView.findViewById(R.id.shipNameTextView);
        TextView shipCostTextView = shipView.findViewById(R.id.shipCostTextView);
        Button selectShipButton = shipView.findViewById(R.id.selectShipButton);

        // Set ship details
        shipNameTextView.setText(ship.getName());
        shipCostTextView.setText("Cost: " + ship.getCost());

        // Set an onClickListener for the select button
        selectShipButton.setOnClickListener(v -> {
            if (highScore >= ship.getCost()) {
                selectedShip = ship.getName(); // Update selected ship
                savePlayerData(); // Save player data
                Toast.makeText(MainActivity.this, "Selected " + selectedShip, Toast.LENGTH_SHORT).show();
                updateUI(); // Update coin balance UI
                startGame();
            } else {
                Toast.makeText(MainActivity.this, "Not enough coins!", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the ship view to the container
        shipContainer.addView(shipView);
    }


    private void savePlayerData() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("high_score", highScore);
        editor.putString("selectedShip", selectedShip);
        editor.apply();
    }


    private void loadPlayerData() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        highScore = gameView.getHighScore();
        Hero.highscore = highScore;
        selectedShip = prefs.getString("selectedShip", "defaultShip");
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}
