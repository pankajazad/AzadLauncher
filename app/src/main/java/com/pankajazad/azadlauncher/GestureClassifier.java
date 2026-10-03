package com.pankajazad.azadlauncher;

final class GestureClassifier {
    private GestureClassifier() { }

    static boolean isDownwardSwipe(
            float horizontalDistance,
            float verticalDistance,
            float verticalVelocity,
            float minimumDistance,
            float minimumVelocity,
            boolean contentCanScrollUp) {
        return !contentCanScrollUp
                && verticalDistance >= minimumDistance
                && verticalVelocity >= minimumVelocity
                && verticalDistance > Math.abs(horizontalDistance);
    }
}
