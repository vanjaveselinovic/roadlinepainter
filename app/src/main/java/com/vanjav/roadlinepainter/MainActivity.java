package com.vanjav.roadlinepainter;

import android.animation.Animator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    RoadLinePainterView roadLinePainterView;
    Button restartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        roadLinePainterView = (RoadLinePainterView) findViewById(R.id.road_line_painter_view);
        restartButton = (Button) findViewById(R.id.game_over_button);
        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                MainActivity.super.recreate();
            }
        });
    }

    @Override
    protected void onPause() {
        roadLinePainterView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        roadLinePainterView.resume();
    }
}
