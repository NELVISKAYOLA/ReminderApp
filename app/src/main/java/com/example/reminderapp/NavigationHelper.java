package com.example.reminderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class NavigationHelper {

    private static final String TAG = "NavigationHelper";

    public static void setupNavigation(final AppCompatActivity activity) {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            
            TextView titleView = toolbar.findViewById(R.id.toolbar_title);
            if (titleView != null) {
                titleView.setText(activity.getTitle());
            }

            // Always use navigation icon for back
            toolbar.setNavigationOnClickListener(v -> activity.getOnBackPressedDispatcher().onBackPressed());
        }

        // Setup Drawer (if present)
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        View menuIcon = activity.findViewById(R.id.iv_menu_toolbar);

        if (drawer != null && menuIcon != null) {
            menuIcon.setVisibility(View.VISIBLE);
            menuIcon.setOnClickListener(v -> {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            });

            if (navigationView != null) {
                // Admin check
                SharedPreferences prefs = activity.getSharedPreferences("user_prefs", activity.MODE_PRIVATE);
                String role = prefs.getString("active_role", "USER");
                MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_admin_console);
                if (adminItem != null) {
                    adminItem.setVisible("ADMIN".equals(role));
                }

                navigationView.setNavigationItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_app_setting) {
                        activity.startActivity(new Intent(activity, AppSettingsActivity.class));
                    } else if (id == R.id.nav_private_reminders) {
                        activity.startActivity(new Intent(activity, PrivateRemindersActivity.class));
                    } else if (id == R.id.nav_insights) {
                        startActivityByName(activity, "com.example.reminderapp.InsightsActivity");
                    } else if (id == R.id.nav_admin_console) {
                        activity.startActivity(new Intent(activity, AdminDashboardActivity.class));
                    } else if (id == R.id.nav_help) {
                        activity.startActivity(new Intent(activity, HelpActivity.class));
                    } else if (id == R.id.nav_feedback) {
                        activity.startActivity(new Intent(activity, FeedbackActivity.class));
                    } else if (id == R.id.nav_about) {
                        activity.startActivity(new Intent(activity, AboutActivity.class));
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
        } else if (menuIcon != null) {
            menuIcon.setVisibility(View.GONE);
        }

        // Bottom Navigation
        BottomNavigationView bottomNav = activity.findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            // Highlighting based on activity type
            if (activity instanceof DashboardActivity) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else if (activity.getClass().getSimpleName().equals("InsightsActivity")) {
                bottomNav.setSelectedItemId(R.id.nav_insights);
            } else if (activity.getClass().getSimpleName().equals("CalendarActivity")) {
                bottomNav.setSelectedItemId(R.id.nav_calendar);
            } else if (activity instanceof ProfileActivity) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                
                // Don't navigate if we are already on that screen
                if (id == R.id.nav_home && activity instanceof DashboardActivity) return true;
                if (id == R.id.nav_insights && activity.getClass().getSimpleName().equals("InsightsActivity")) return true;
                if (id == R.id.nav_calendar && activity.getClass().getSimpleName().equals("CalendarActivity")) return true;
                if (id == R.id.nav_profile && activity instanceof ProfileActivity) return true;

                if (id == R.id.nav_home) {
                    startActivityWithFlag(activity, DashboardActivity.class);
                } else if (id == R.id.nav_insights) {
                    startActivityByName(activity, "com.example.reminderapp.InsightsActivity");
                } else if (id == R.id.nav_calendar) {
                    startActivityByName(activity, "com.example.reminderapp.CalendarActivity");
                } else if (id == R.id.nav_profile) {
                    startActivityWithFlag(activity, ProfileActivity.class);
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

    private static void startActivityWithFlag(AppCompatActivity activity, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }

    private static void startActivityByName(AppCompatActivity activity, String className) {
        try {
            Intent intent = new Intent(activity, Class.forName(className));
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Activity class not found: " + className, e);
        }
    }
}
