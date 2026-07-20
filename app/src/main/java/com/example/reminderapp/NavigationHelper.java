package com.example.reminderapp;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class NavigationHelper {

    public static void setupNavigation(final AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
        }

        // Setup Drawer (if present)
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        if (drawer != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, drawer, toolbar, R.string.app_name, R.string.app_name);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setHomeButtonEnabled(true);
            }

            if (navigationView != null) {
                navigationView.setNavigationItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_app_setting) {
                        activity.startActivity(new Intent(activity, AppSettingsActivity.class));
                    } else if (id == R.id.nav_private_reminders) {
                        activity.startActivity(new Intent(activity, PrivateRemindersActivity.class));
                    } else if (id == R.id.nav_other_reminders) {
                        activity.startActivity(new Intent(activity, InsightsActivity.class));
                    } else if (id == R.id.nav_feedback) {
                        activity.startActivity(new Intent(activity, FeedbackActivity.class));
                    } else if (id == R.id.nav_privacy) {
                        activity.startActivity(new Intent(activity, PrivacyPolicyActivity.class));
                    } else if (id == R.id.nav_terms) {
                        activity.startActivity(new Intent(activity, TermsPolicyActivity.class));
                    } else if (id == R.id.nav_theme) {
                        activity.startActivity(new Intent(activity, ThemeActivity.class));
                    }
                    drawer.closeDrawers();
                    return true;
                });
            }
        }

        // Bottom Navigation
        BottomNavigationView bottomNav = activity.findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            // Highlighting
            if (activity instanceof DashboardActivity) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else if (activity instanceof InsightsActivity) {
                bottomNav.setSelectedItemId(R.id.nav_insights);
            } else if (activity instanceof CalendarActivity) {
                bottomNav.setSelectedItemId(R.id.nav_calendar);
            } else if (activity instanceof ProfileActivity) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                Intent intent = null;
                if (id == R.id.nav_home && !(activity instanceof DashboardActivity)) {
                    intent = new Intent(activity, DashboardActivity.class);
                } else if (id == R.id.nav_insights && !(activity instanceof InsightsActivity)) {
                    intent = new Intent(activity, InsightsActivity.class);
                } else if (id == R.id.nav_calendar && !(activity instanceof CalendarActivity)) {
                    intent = new Intent(activity, CalendarActivity.class);
                } else if (id == R.id.nav_profile && !(activity instanceof ProfileActivity)) {
                    intent = new Intent(activity, ProfileActivity.class);
                }

                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                    return true;
                }
                return id != R.id.nav_placeholder;
            });
        }

        // FAB
        FloatingActionButton fabAdd = activity.findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                activity.startActivity(new Intent(activity, AddeventActivity.class));
            });
        }
    }
}
