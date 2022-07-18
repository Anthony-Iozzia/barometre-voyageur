package voyageur.barometre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor pressure;

    private TextView tvPressure;
    private TextView tvAltitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        tvPressure = findViewById(R.id.value_pressure);
        tvAltitude = findViewById(R.id.value_altitude);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float millibarsOfPressure = event.values[0];
        float default_ambient_temp = 27;
        double altitude = ((273 + default_ambient_temp) / 0.0065) * (1 - Math.pow((millibarsOfPressure / 1013.25),1/5.255));
        int pressureRounded = Math.round(millibarsOfPressure);
        int altitudeRounded = Math.toIntExact(Math.round(altitude));
        tvPressure.setText(String.valueOf(pressureRounded));
        tvAltitude.setText(String.valueOf(altitudeRounded));
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}