# Ecommerce Speedbump

An aggressive behavioral intervention app designed to curb impulse spending on major Indian e-commerce and quick-commerce platforms.

## 🚀 How to get the APK (via GitHub Actions)

Since you don't have a local Android build environment, I have set up **GitHub Actions** to build the APK for you automatically.

### Step 1: Create a GitHub Repository
1. Go to [github.com/new](https://github.com/new).
2. Name it `ecommerce-speedbump`.
3. Follow the instructions to push this code to your new repository.

### Step 2: Trigger the Build
- Once you push the code, GitHub will automatically start a "Workflow".
- Click on the **Actions** tab in your GitHub repository.
- You will see a workflow named "Build Android APK" running.

### Step 3: Download the APK
1. Wait for the workflow to finish (usually takes 2-3 minutes).
2. Click on the completed run.
3. Scroll down to the **Artifacts** section.
4. Download the `app-debug` zip file. Inside, you will find `app-debug.apk`.

## 🛠 Setup on Phone

1. **Install**: Sideload the `app-debug.apk` onto your Android phone.
2. **Open the App**: Launch "Ecommerce Speedbump".
3. **Permissions**:
    - Tap **Enable Overlay Permission** and find the app in the list to toggle it ON.
    - Tap **Enable Accessibility Service**, find "Ecommerce Speedbump" under installed services, and toggle it ON.

## 🛡 Features
- **Monitors**: Amazon, Flipkart, Myntra, Swiggy, Zomato, Blinkit, Zepto, and more.
- **Calculates**: Shows how many hours of work (at ₹500/hr) it takes to pay for your cart.
- **Speedbump**: A 60-second un-dismissible lock screen that forces you to contemplate the purchase.
