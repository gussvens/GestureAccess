# Gesture access
SDK versions: 28-30

URL to website: https://gustavhsvensson.wixsite.com/website/gesture-access

How to run application on a device or emulator (we are not sure if the gesture works on all emulators as the app uses onboard imu cards):
1. Download project and open it in Android Studio.
2. Sync Gradle files if it is not done automatically.
3. Using an emulator or device with at least the minimum SDK version install the application.
4. The design is primarily the prototype widget, so you are required to add the widget "Gesture access" to a home screen page.
5. When the widget is placed, a configuration activity is open. Pick what app to affect from the list of installed applications.
6. The widget now host and displaysthe chosen app's icon. Tapping the app icon will open the 'Gesture access' application, where a gesture prompt will be displayed as well as the difficuty level and the current progress.
7. Before the timer runs out, perform the gesture (we are not sure this is possible on an emulator) until the app opens. The phone vibrates when an accepted gesture is performed.
