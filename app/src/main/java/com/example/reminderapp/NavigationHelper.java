package com.example.reminderapp;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavigationHelper {

    public static void setupNavigation(final AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
        }

        BottomNavigationView bottomNav = activity.findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            if (activity instanceof DashboardActivity) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else if (activity instanceof CalendarActivity) {
                bottomNav.setSelectedItemId(R.id.nav_calendar);
            } else if (activity instanceof InsightsActivity) {
                bottomNav.setSelectedItemId(R.id.nav_insights);
            } else if (activity instanceof ProfileActivity) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    if (!(activity instanceof DashboardActivity)) {
                        Intent intent = new Intent(activity, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        activity.startActivity(intent);
                    }
                    return true;
                } else if (id == R.id.nav_calendar) {
                    if (!(activity instanceof CalendarActivity)) {
                        Intent intent = new Intent(activity, CalendarActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        activity.startActivity(intent);
                    }
                    return true;
                } else if (id == R.id.nav_insights) {
                    if (!(activity instanceof InsightsActivity)) {
                        Intent intent = new Intent(activity, InsightsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        activity.startActivity(intent);
                    }
                    return true;
                } else if (id == R.id.nav_profile) {
                    if (!(activity instanceof ProfileActivity)) {
                        Intent intent = new Intent(activity, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        activity.startActivity(intent);
                    }
                    return true;
                }
                return false;
            });
        }

        FloatingActionButton fabAdd = activity.findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                activity.startActivity(new Intent(activity, AddeventActivity.class));
            });
        }
    }

    public static boolean handleOptionsMenu(AppCompatActivity activity, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_terms) {
            activity.startActivity(new Intent(activity, TermsPolicyActivity.class));
            return true;
        } else if (id == R.id.action_theme) {
            activity.startActivity(new Intent(activity, ThemeActivity.class));
            return true;
        } else if (id == R.id.action_feedback) {
            activity.startActivity(new Intent(activity, FeedbackActivity.class));
            return true;
        } else if (id == R.id.action_privacy) {
            activity.startActivity(new Intent(activity, PrivacyPolicyActivity.class));
            return true;
        }
        return false;
    }
}
