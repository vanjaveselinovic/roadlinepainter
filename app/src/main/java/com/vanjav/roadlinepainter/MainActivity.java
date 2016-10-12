package com.vanjav.roadlinepainter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    RoadLinePainterView roadLinePainterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        roadLinePainterView = (RoadLinePainterView) findViewById(R.id.road_line_painter_view);
    }

    @Override
    protected void onPause() {
        super.onPause();
        roadLinePainterView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        roadLinePainterView.resume();
    }
}
