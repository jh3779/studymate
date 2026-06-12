package com.example.studymate;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(getColor(R.color.study_bg));
        getWindow().setNavigationBarColor(getColor(R.color.study_bg));
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applySystemBarInsets();
    }

    private void applySystemBarInsets() {
        View content = findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) {
            return;
        }

        ViewGroup contentGroup = (ViewGroup) content;
        if (contentGroup.getChildCount() == 0) {
            return;
        }

        View root = contentGroup.getChildAt(0);
        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );
            view.setPadding(
                    initialLeft + insets.left,
                    initialTop + insets.top,
                    initialRight + insets.right,
                    initialBottom + insets.bottom
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    protected void goTo(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    @SuppressWarnings("deprecation")
    protected void switchTopLevel(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    protected void goToAndClear(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    protected void bindClick(int viewId, View.OnClickListener listener) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    protected void showShortToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showDeleteConfirmation(
            String title,
            String message,
            Runnable onConfirm
    ) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_delete);
        dialog.setCancelable(true);

        TextView titleView = dialog.findViewById(R.id.deleteDialogTitle);
        TextView messageView = dialog.findViewById(R.id.deleteDialogMessage);
        Button cancelButton = dialog.findViewById(R.id.deleteDialogCancel);
        Button confirmButton = dialog.findViewById(R.id.deleteDialogConfirm);

        titleView.setText(title);
        messageView.setText(message);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });

        dialog.setOnShowListener(unused -> {
            Window window = dialog.getWindow();
            if (window == null) {
                return;
            }
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            attributes.dimAmount = 0.65f;
            window.setAttributes(attributes);
            window.getDecorView().setPadding(dpToPx(24), 0, dpToPx(24), 0);
        });
        dialog.show();
    }

    protected int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
