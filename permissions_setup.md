# Permissions Setup Guide

To test the **Ecommerce Speedbump** app, you must manually grant two high-level Android permissions after sideloading the APK.

## 1. Display Over Other Apps (Overlay)
This allows the "Speedbump" to appear over e-commerce apps.

1. Open **Settings**.
2. Go to **Apps** (or **Apps & Notifications**).
3. Tap on **Special app access** (usually under 'Advanced' or the three-dot menu).
4. Select **Display over other apps**.
5. Find **Ecommerce Speedbump** in the list.
6. Toggle **Allow display over other apps** to **ON**.

---

## 2. Accessibility Service
This allows the app to read checkout buttons and price totals.

1. Open **Settings**.
2. Go to **Accessibility**.
3. Under **Downloaded Apps** (or **Installed Services**), find **Ecommerce Speedbump**.
4. Tap it and toggle **Use Ecommerce Speedbump** to **ON**.
5. Tap **Allow** on the system warning dialog.

---

## 3. Verify Setup
- Open the app once to ensure the "MainActivity" runs.
- Open **Amazon** or **Flipkart**, go to a checkout page, and wait for the "Speedbump" to trigger!
