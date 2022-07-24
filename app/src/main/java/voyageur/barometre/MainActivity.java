package voyageur.barometre;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final double DEFAULT_PRESSURE_SEA_LEVEL = 1013.25;
    private static final double ALTITUDE_SEA_LEVEL = 0.0;
    private static final double DEFAULT_AMBIENT_TEMP = 15.0;
    private static final double KELVIN_CST = 273.15;

    private double pressureRealTime = DEFAULT_PRESSURE_SEA_LEVEL;
    private double altitudeCalibrationMode1 = ALTITUDE_SEA_LEVEL;
    private double pressureSeaLevelMode1 = DEFAULT_PRESSURE_SEA_LEVEL;
    
    private SensorManager sensorManager;
    private Sensor sensorPressure;

    private EditText fieldAltitudeCalibrationMode1;
    private Button buttonAltitudeCalibrationMode1;
    private TextView tvPressure;
    private TextView tvPressureSeaLevelMode1;
    private TextView tvAltitudeMode0;
    private TextView tvAltitudeMode1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();

        TextView tvPressureSeaLevelMode0 = findViewById(R.id.value_pressure_sea_level_mode0);
        tvPressureSeaLevelMode0.setText(String.format("%.2f", DEFAULT_PRESSURE_SEA_LEVEL));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        tvPressure = findViewById(R.id.value_pressure);
        tvAltitudeMode0 = findViewById(R.id.value_altitude_mode0);
        tvAltitudeMode1 = findViewById(R.id.value_altitude_mode1);
        tvPressureSeaLevelMode1 = findViewById(R.id.value_pressure_sea_level_mode1);

        fieldAltitudeCalibrationMode1 = (EditText) findViewById(R.id.field_altitude_calibration_mode1);
        buttonAltitudeCalibrationMode1 = (Button) findViewById(R.id.button_altitude_calibration_mode1);

        buttonAltitudeCalibrationMode1.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        try {
                            altitudeCalibrationMode1 = Integer.valueOf(fieldAltitudeCalibrationMode1.getText().toString());
                        }
                        catch(NumberFormatException ex) {
                            String toastTextError = context.getResources().getString(R.string.error_no_value_specified);
                            Toast.makeText(context, toastTextError, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String toastText = context.getResources().getString(R.string.toast_altitude_calibrated) + String.format("%.0f", altitudeCalibrationMode1) + context.getResources().getString(R.string.unit_altitude);
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();

                        // Compute pressure at sea level
                        pressureSeaLevelMode1 = pressureRealTime / (Math.pow(1.0 - (0.0065 * altitudeCalibrationMode1)/(KELVIN_CST + DEFAULT_AMBIENT_TEMP),5.255));
                        tvPressureSeaLevelMode1.setText(String.format("%.2f", pressureSeaLevelMode1));
                    }
                });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        pressureRealTime = event.values[0]; // Pressure in hPa
        tvPressure.setText(String.format("%.2f", pressureRealTime));

        double altitudeMode0 = computeAltitude(pressureRealTime, DEFAULT_PRESSURE_SEA_LEVEL, DEFAULT_AMBIENT_TEMP);
        tvAltitudeMode0.setText(String.format("%.2f", altitudeMode0));

        double altitudeMode1 = computeAltitude(pressureRealTime, pressureSeaLevelMode1, DEFAULT_AMBIENT_TEMP);
        tvAltitudeMode1.setText(String.format("%.2f", altitudeMode1));
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        sensorManager.registerListener(this, sensorPressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private double computeAltitude(double pressure, double pressureSeaLevel, double ambient_temp) {
        return ((KELVIN_CST + ambient_temp) / 0.0065) * (1.0 - Math.pow((pressure / pressureSeaLevel), 1.0 / 5.255));
    }
}