package com.example.academicapp.activities;

import android.app.Activity;
import android.content.Intent;

import com.example.academicapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Utilidad centralizada para mantener la navegacion inferior coherente entre pantallas.
 */
public final class NavigationHelper {

    private NavigationHelper() {
    }

    public static void setup(Activity activity, BottomNavigationView navigationView, int selectedItemId) {
        navigationView.setSelectedItemId(selectedItemId);
        navigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == selectedItemId) {
                return true;
            }

            navigateTo(activity, itemId, selectedItemId);
            return false;
        });
    }

    private static void navigateTo(Activity activity, int itemId, int selectedItemId) {
        Class<?> target = getTargetActivity(itemId);
        if (target == null || itemId == selectedItemId) {
            return;
        }

        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    private static Class<?> getTargetActivity(int itemId) {
        if (itemId == R.id.nav_home) {
            return MainActivity.class;
        }

        if (itemId == R.id.nav_subjects) {
            return AsignaturasActivity.class;
        }

        if (itemId == R.id.nav_notes) {
            return NotasActivity.class;
        }

        if (itemId == R.id.nav_stats) {
            return EstadisticasActivity.class;
        }

        if (itemId == R.id.nav_profile) {
            return PerfilActivity.class;
        }

        return null;
    }
}
