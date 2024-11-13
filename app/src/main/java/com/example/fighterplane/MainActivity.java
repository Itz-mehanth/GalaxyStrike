package com.example.fighterplane;

import com.example.fighterplane.components.*;
import static com.example.fighterplane.GameView.decodeSampledBitmapFromResource;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout startLayout, storeLayout;
    private ViewFlipper viewFlipper, contentFlipper;
    private Button startButton, storeButton, backButton;
    private TextView coinBalanceText, highScoreText;
    private String selectedShip = "Speed Ship";
    private String selectedMap = "space";

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
        viewFlipper = findViewById(R.id.viewFlipper); // Initialize ViewFlipper
        contentFlipper = findViewById(R.id.contentFlipper); // Initialize ViewFlipper

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
        // Hide the start and store layouts
        startLayout.setVisibility(View.GONE);
        storeLayout.setVisibility(View.GONE);
        viewFlipper.setVisibility(View.GONE);

        // Show the GameView and start the game
        gameView.setVisibility(View.VISIBLE);

        // Pass the selected ship and map to GameView
        gameView.setSelectedShip(selectedShip);
        gameView.setSelectedMap(selectedMap);
        gameView.startGame();

        Toast.makeText(this, "Starting game with " + selectedShip + " on " + selectedMap, Toast.LENGTH_SHORT).show();
    }

    private void onBackToHomeClicked() {
        // Show the start layout to go back to the main screen
        startLayout.setVisibility(View.VISIBLE);

        // Hide the store layout and GameView
        storeLayout.setVisibility(View.GONE);
        gameView.setVisibility(View.GONE);
    }

    private void updateUI() {
        highScoreText.setText("High Score: " + highScore);
        coinBalanceText.setText("Coins: " + currentCoins);
    }

    private void openStore() {
        startLayout.setVisibility(View.GONE);
        storeLayout.setVisibility(View.VISIBLE);
        viewFlipper.setVisibility(View.VISIBLE); // Show ViewFlipper when entering store

        // Populate ship and map stores
        populateShipStore();
        populateMapStore();

        // Setup button listeners for navigation
        Button btnShips = findViewById(R.id.btnShips);
        Button btnMaps = findViewById(R.id.btnMaps);

        btnShips.setOnClickListener(v -> {
            contentFlipper.setDisplayedChild(0); // Show ships
        });

        btnMaps.setOnClickListener(v -> {
            contentFlipper.setDisplayedChild(1); // Show maps
        });
    }

    private void populateShipStore() {
        LinearLayout shipContainer = findViewById(R.id.shipContainer);
        shipContainer.removeAllViews(); // Clear existing views

        // Create example ships
        List<Ship> ships = new ArrayList<>();
        ships.add(new Ship("Speed Ship", 0, R.drawable.speed_ship));
        ships.add(new Ship("Shield Ship", 20000, R.drawable.shield_ship));
        ships.add(new Ship("Power Ship", 30000, R.drawable.plane));
        ships.add(new Ship("Speed Ship", 0, R.drawable.speed_ship));
        ships.add(new Ship("Shield Ship", 20000, R.drawable.shield_ship));

        // Add each ship to the store
        for (Ship ship : ships) {
            addShipToStore(ship, shipContainer);
        }
    }

    private void populateMapStore() {
        LinearLayout mapContainer = findViewById(R.id.mapContainer);
        mapContainer.removeAllViews(); // Clear existing views

        // Add sample maps to the list
        List<Map> mapList = new ArrayList<>();
        mapList.add(new Map("ocean", R.drawable.oceanbackground));
        mapList.add(new Map("space", R.drawable.space));
        mapList.add(new Map("ocean", R.drawable.oceanbackground));
        mapList.add(new Map("space", R.drawable.space));
        mapList.add(new Map("ocean", R.drawable.oceanbackground));

        for (Map map : mapList) {
            addMapToStore(map, mapContainer);
        }
    }

    private void addShipToStore(Ship ship, LinearLayout shipContainer) {
        // Inflate the item_ship layout
        View shipView = getLayoutInflater().inflate(R.layout.item_ship, shipContainer, false);

        // Find views in the inflated layout
        ImageView shipImageView = shipView.findViewById(R.id.shipImageView);
        TextView shipNameTextView = shipView.findViewById(R.id.shipNameTextView);
        TextView shipCostTextView = shipView.findViewById(R.id.shipCostTextView);
        Button selectShipButton = shipView.findViewById(R.id.selectShipButton);

        // Set ship details
        shipNameTextView.setText(ship.getName());
        shipCostTextView.setText("Cost: " + ship.getCost());
        shipImageView.setImageResource(ship.getImageResourceId());

        // Set an onClickListener for the select button
        selectShipButton.setOnClickListener(v -> {
            if (highScore >= ship.getCost()) {
                selectedShip = ship.getName(); // Update selected ship
                gameView.planeBitmap = decodeSampledBitmapFromResource(getResources(), ship.getImageResourceId(), 70, 70);
                savePlayerData(); // Save player data
                Toast.makeText(MainActivity.this, "Selected " + selectedShip, Toast.LENGTH_SHORT).show();
                updateUI(); // Update coin balance UI
            } else {
                Toast.makeText(MainActivity.this, "Not enough coins!", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the ship view to the container
        shipContainer.addView(shipView);
    }

    private void addMapToStore(Map map, LinearLayout mapContainer) {
        // Inflate the item_map layout
        View mapView = getLayoutInflater().inflate(R.layout.item_map, mapContainer, false);

        // Find views in the inflated layout
        ImageView mapImageView = mapView.findViewById(R.id.mapImageView);
        TextView mapNameTextView = mapView.findViewById(R.id.mapNameTextView);
        Button selectMapButton = mapView.findViewById(R.id.selectMapButton);

        // Set map details
        mapNameTextView.setText(map.getName());
        mapImageView.setImageResource(map.getImageResId());

        // Set an onClickListener for the select button
        selectMapButton.setOnClickListener(v -> {
            selectedMap = map.getName(); // Update selected map
            gameView.backgroundBitmap = decodeSampledBitmapFromResource(getResources(), map.getImageResId(), 3898, 2000); // Ensure this is 3 times the width of the screen

            savePlayerData(); // Save player data
            Toast.makeText(MainActivity.this, "Selected map: " + selectedMap, Toast.LENGTH_SHORT).show();

            startGame(); // Start the game with the selected map
        });

        // Add the map view to the container
        mapContainer.addView(mapView);
    }

    private void savePlayerData() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("high_score", highScore);
        editor.putString("selectedShip", selectedShip);
        editor.putString("selectedMap", selectedMap); // Save selected map
        editor.apply();
    }

    private void loadPlayerData() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        highScore = gameView.getHighScore();
//        selectedShip = prefs.getString("selectedShip", "Speed Ship"); // Default ship
//        selectedMap = prefs.getString("selectedMap", "Desert Dunes"); // Default map
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
            }
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(); // Update UI every time the activity resumes
    }
}
