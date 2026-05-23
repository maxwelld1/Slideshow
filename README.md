# Slideshow

Home screen Wallpaper slideshow -- targeting Android 16.0.

This project aspires to M.I.T. multi-screen recommendations.

Does not comply with Android prevailing conventions about extensive background work.

Rotating a drawable bitmap in a foreground service using a countdown timer. 

> Direct boot mode allows wallpaper change before the user unlocks the device.
>
>> The service needs to be explicitly started by using a Pendingintent. Keeping track of service status and started intents using shared state shared preferences.

# 🧪 Test Harness
Test harness for looping through overlay displays / changing virtual display properties.
```
CYCLES=10
for ((i=1; i<=CYCLES; i++))
do
    echo "=== Cycle $i: Adding virtual display ==="
 🧩   adb shell settings put global overlay_display_devices "948x1048/342"
    sleep 2
echo "=== Cycle $i: Changing display resolution ==="
 🔍   adb shell wm size 2520x1080
    sleep 2
echo "=== Cycle $i: Changing display density ==="
    adb shell wm density 397
    sleep 2
echo "=== Cycle $i: Resetting display metrics ==="
    adb shell wm size reset
    adb shell wm density reset
    sleep 2
echo "=== Cycle $i: Removing virtual display ==="
    adb shell settings put global overlay_display_devices ""
    sleep 2
echo "=== Cycle $i complete ==="
    sleep 1
🔁 done
echo "=== All cycles complete ==="
```
