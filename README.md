# Blue Light Filter

An Android application designed to reduce eye strain by applying a configurable red hue and dimming the screen below the system minimum.

## Features

*   **Adjustable Red Hue:** Control the intensity of the red overlay with a simple slider.
*   **Deep Screen Dimmer:** Dim the screen to 20%, 40%, 60%, 80%, or 90% brightness, going darker than the system's default minimum.
*   **Full-Screen Coverage:** The filter applies to the entire screen, including the status bar and notification shade.
*   **Dark Mode UI:** A clean, dark interface for comfortable use at night.

## Setup

This application uses an Android **Accessibility Service** to draw the filter over the entire screen.

On first launch, the app will prompt you to enable the service in your device's **Settings > Accessibility** menu. This is a one-time setup step required for the filter to function correctly.