# Galaxy Strike - Conqueror of the planets

## 🎮 Overview
**Fighter Plane Game** is a thrilling 2D shooter game built in Java for Android (supports SDK 31 to 34). Players control a fighter plane through two unique maps, facing alien and oceanic enemies, collecting power-ups, and ultimately battling powerful bosses. The game leverages device sensors, including the gyroscope and accelerometer, to create an immersive experience where players can control their plane by rotating their phone and tap to shoot.

## 📲 Game Flow
1. **Splash Screen**: Displays the game logo briefly before transitioning to the main menu.
2. **Main Menu**: Players can choose to start the game directly or visit the in-game store.
3. **In-Game Store**:
   - **Planes**: Three types of planes are available, unlocked based on the player's high score. Each plane has unique attributes:
     - **Increased Resistance**: Reduces health loss when hit by enemies.
     - **Enhanced Speed**: Boosts the plane's movement speed.
     - **Special Attack**: A unique power for greater combat impact.
   - **Maps**: Two maps are available:
     - **Space**: Features alien enemies, an alien monster, and a final boss.
     - **Ocean**: Introduces oceanic creatures like sharks, squids, and a final battle against Poseidon, the god of the ocean.
     - Maps are unlocked sequentially, with new maps accessible once the previous one is completed.

## 🌍 Game Features
- **Dynamic Levels**:
  - **Space Map**: Encounter aliens, alien monsters, and a formidable alien king.
  - **Ocean Map**: Battle oceanic creatures such as octopuses, squids, sharks, and a final showdown with Poseidon.
- **Health and Power-Ups**:
  - Health is displayed at the top of the screen and is impacted by enemy attacks.
  - **Power-Up Types**:
    - **Health Boost**: Restores player's health.
    - **Triple Bullets**: Increases firing power by tripling bullets per shot.
    - **Double Bullets**: Doubles bullets per shot.
    - **Bullet Style Change**: Alters the bullet's style for a varied attack.
    - **Debuff Power-Ups**: Certain power-ups reduce health, adding a strategic element where players must avoid these items.
  - **Reload Mechanic**: After every 100 bullets, a brief reload delay occurs.
- **Advanced Controls**: Utilize the gyroscope and accelerometer to control the plane by tilting the device, and tap to shoot bullets.

## 📦 Installation
1. Download the APK from the [Releases](https://github.com/Itz-mehanth/FighterPlane/releases).
2. Enable *Install from Unknown Sources* on your Android device if prompted.
3. Install the APK and launch the game.

## 🎮 How to Play
1. **Start the Game** from the main menu.
2. **Control Your Plane** by tilting your phone.
3. **Shoot Enemies** by tapping the screen.
4. **Avoid Harmful Power-Ups** and **collect beneficial Power-Ups** to stay ahead.
5. **Battle the Bosses** at the end of each map for a chance to unlock the next map.

## 🔧 Building the APK
1. Open the project in Android Studio.
2. Go to **Build > Generate Signed Bundle / APK**.
3. Follow the prompts to enter your keystore information for release mode.
4. Once complete, you can install the APK on Android devices.

## 📈 Future Enhancements
- Additional maps and bosses for more variety.
- New power-ups and debuffs for expanded gameplay strategies.

## 📝 License
This project is licensed under the MIT License.
