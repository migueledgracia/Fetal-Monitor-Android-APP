package com.example.fetalmonitor;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.util.PlotStatistics;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.*;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

// Monitor the phone's orientation sensor and plot the resulting azimuth pitch and roll values.
// See: http://developer.android.com/reference/android/hardware/SensorEvent.html
public class HeartRateScreen extends Activity implements SensorEventListener, OnTouchListener
{

    private static final int HISTORY_SIZE = 300;            // number of points to plot in history
    private SensorManager sensorMgr = null;
    private Sensor orSensor = null;

    //private XYPlot aprLevelsPlot = null;
    private XYPlot aprHistoryPlot = null;

    private CheckBox hwAcceleratedCb;
    private CheckBox showFpsCb;
    //private SimpleXYSeries aprLevelsSeries = null;
    private SimpleXYSeries aLvlSeries;
    private SimpleXYSeries pLvlSeries;
    //private SimpleXYSeries rLvlSeries;
    private SimpleXYSeries azimuthHistorySeries = null;
    private SimpleXYSeries pitchHistorySeries = null;
    //private SimpleXYSeries rollHistorySeries = null;
    
//////////////////////////////////////////////////////
    private SimpleXYSeries[] series = null;
    private static final int SERIES_SIZE = 200;
/////////////////////////////////////////////////////
    
    private PointF minXY;
    private PointF maxXY;

    private Redrawer redrawer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_monitor);

		Bundle extras = getIntent().getExtras(); 
		
		if (extras != null) 
		{
    		TextView titlePatientName = (TextView)findViewById(R.id.titlePatientName);
    		titlePatientName.setText(extras.getString("PatientName"));
		}
		

		
        aLvlSeries = new SimpleXYSeries("A");
        pLvlSeries = new SimpleXYSeries("P");
        //rLvlSeries = new SimpleXYSeries("R");

        // setup the APR History plot:
        aprHistoryPlot = (XYPlot) findViewById(R.id.aprHistoryPlot);
        aprHistoryPlot.setOnTouchListener(this);
        
///////////////////////////////////////////////////////////////////////
        aprHistoryPlot.getGraphWidget().setTicksPerRangeLabel(2);
        aprHistoryPlot.getGraphWidget().setTicksPerDomainLabel(2);
        aprHistoryPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        aprHistoryPlot.getGraphWidget().setRangeValueFormat(
                new DecimalFormat("#####"));
        aprHistoryPlot.getGraphWidget().setDomainValueFormat(
                new DecimalFormat("#####.#"));
        aprHistoryPlot.getGraphWidget().setRangeLabelWidth(25);
        aprHistoryPlot.setRangeLabel("");
        aprHistoryPlot.setDomainLabel("");
///////////////////////////////////////////////////////////////////////
        
        azimuthHistorySeries = new SimpleXYSeries("Az.");
        azimuthHistorySeries.useImplicitXVals();
        pitchHistorySeries = new SimpleXYSeries("Pitch");
        pitchHistorySeries.useImplicitXVals();
        //rollHistorySeries = new SimpleXYSeries("Roll");
        //rollHistorySeries.useImplicitXVals();

        aprHistoryPlot.setRangeBoundaries(-180, 359, BoundaryMode.FIXED);
        aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        aprHistoryPlot.addSeries(azimuthHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 100, 200), null, null, null));
        aprHistoryPlot.addSeries(pitchHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 200, 100), null, null, null));
        //aprHistoryPlot.addSeries(rollHistorySeries,
             //   new LineAndPointFormatter(
                    //    Color.rgb(200, 100, 100), null, null, null));
        aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        aprHistoryPlot.setDomainStepValue(HISTORY_SIZE/10);
        aprHistoryPlot.setTicksPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("Sample Index");
        aprHistoryPlot.getDomainLabelWidget().pack();
        aprHistoryPlot.setRangeLabel("Angle (Degs)");
        aprHistoryPlot.getRangeLabelWidget().pack();

        aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
        aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

        // setup checkboxes:
        hwAcceleratedCb = (CheckBox) findViewById(R.id.hwAccelerationCb);
        final PlotStatistics levelStats = new PlotStatistics(1000, false);
        final PlotStatistics histStats = new PlotStatistics(1000, false);

        aprHistoryPlot.addListener(histStats);
        hwAcceleratedCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    aprHistoryPlot.setLayerType(View.LAYER_TYPE_NONE, null);
                } else {
                    aprHistoryPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }
        });

        showFpsCb = (CheckBox) findViewById(R.id.showFpsCb);
        showFpsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                levelStats.setAnnotatePlotEnabled(b);
                histStats.setAnnotatePlotEnabled(b);
            }
        });

        // register for orientation sensor events:
        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_ORIENTATION)) {
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                orSensor = sensor;
            }
        }

        // if we can't access the orientation sensor then exit:
        if (orSensor == null) {
            System.out.println("Failed to attach to orSensor.");
            cleanup();
        }

        sensorMgr.registerListener(this, orSensor, SensorManager.SENSOR_DELAY_UI);

        redrawer = new Redrawer(
                Arrays.asList(new Plot[]{aprHistoryPlot}),
                100, false);
        
////////////////////////////////////////////////////////////////////////////////   
        aprHistoryPlot.calculateMinMaxVals();
        minXY = new PointF(aprHistoryPlot.getCalculatedMinX().floatValue(),
        		aprHistoryPlot.getCalculatedMinY().floatValue());
        maxXY = new PointF(aprHistoryPlot.getCalculatedMaxX().floatValue()*2,
        		aprHistoryPlot.getCalculatedMaxY().floatValue());
//////////////////////////////////////////////////////////////////////////////
        
        addListenerOnButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        redrawer.start();
    }

    @Override
    public void onPause() {
        redrawer.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        redrawer.finish();
        super.onDestroy();
    }

    private void cleanup() {
        // aunregister with the orientation sensor before exiting:
        sensorMgr.unregisterListener(this);
        finish();
    }

////////////////////////////////////////////////////////////////
/////////////////////////Button Listener///////////////////////
//////////////////////////////////////////////////////////////
    public void addListenerOnButton() 
    {


    }
    
////////////////////////////////////////////////////////////////
////////////////////Gyroscope Sensor///////////////////////////
//////////////////////////////////////////////////////////////
    // Called whenever a new orSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {

        // update level data:
        aLvlSeries.setModel(Arrays.asList(
                new Number[]{sensorEvent.values[0]}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        pLvlSeries.setModel(Arrays.asList(
                        new Number[]{sensorEvent.values[1]}),
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        //rLvlSeries.setModel(Arrays.asList(
         //               new Number[]{sensorEvent.values[2]}),
         //               SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        // get rid the oldest sample in history:
        /*
        if (azimuthHistorySeries.size() > HISTORY_SIZE) {
            //rollHistorySeries.removeFirst();
            pitchHistorySeries.removeFirst();
            azimuthHistorySeries.removeFirst();
        }
        */
        // add the latest history sample:
        azimuthHistorySeries.addLast(null, sensorEvent.values[0]);
        pitchHistorySeries.addLast(null, sensorEvent.values[1]);
        //rollHistorySeries.addLast(null, sensorEvent.values[2]);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }
    
    // Definition of the touch states
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;
    
    PointF firstFinger;
    float distBetweenFingers;
    boolean stopThread = false;
    
    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                stopThread = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second finger
                distBetweenFingers = spacing(event);
                // the distance check is done to avoid false alarms
                if (distBetweenFingers > 5f) {
                    mode = TWO_FINGERS_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ONE_FINGER_DRAG) {
                    PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
                    scroll(oldFirstFinger.x - firstFinger.x);
                    aprHistoryPlot.setDomainBoundaries(0, maxXY.x*2,
                            BoundaryMode.FIXED);
                    aprHistoryPlot.redraw();

                } else if (mode == TWO_FINGERS_DRAG) {
                    float oldDist = distBetweenFingers;
                    distBetweenFingers = spacing(event);
                    zoom(oldDist / distBetweenFingers);
                    aprHistoryPlot.setDomainBoundaries(minXY.x, maxXY.x,
                            BoundaryMode.FIXED);
                    aprHistoryPlot.redraw();
                }
                break;
        }
        return true;
    }

    
    private void scroll(float pan) {
        float domainSpan = maxXY.x - minXY.x;
        float step = domainSpan / aprHistoryPlot.getWidth();
        float offset = pan * step;
        minXY.x = minXY.x + offset;
        maxXY.x = maxXY.x + offset;
        clampToDomainBounds(domainSpan);
    }

    private void zoom(float scale) {
        float domainSpan = maxXY.x - minXY.x;
        float domainMidPoint = maxXY.x - domainSpan / 2.0f;
        float offset = domainSpan * scale / 2.0f;

        minXY.x = domainMidPoint - offset;
        maxXY.x = domainMidPoint + offset;

        minXY.x = Math.min(minXY.x, aLvlSeries.getX(aLvlSeries.size() - 3)
                .floatValue());
        maxXY.x = Math.max(maxXY.x, aLvlSeries.getX(1).floatValue());
        clampToDomainBounds(domainSpan);
    }
    
    private void clampToDomainBounds(float domainSpan) {
        float leftBoundary = aLvlSeries.getX(0).floatValue();
        float rightBoundary = aLvlSeries.getX(aLvlSeries.size() - 1).floatValue();
        // enforce left scroll boundary:
        if (minXY.x < leftBoundary) {
            minXY.x = leftBoundary;
            maxXY.x = leftBoundary + domainSpan;
        } else if (maxXY.x > aLvlSeries.getX(aLvlSeries.size() - 1).floatValue()) {
            maxXY.x = rightBoundary;
            minXY.x = rightBoundary - domainSpan;
        }
    }
    
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
}

