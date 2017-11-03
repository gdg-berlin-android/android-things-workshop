package de.berlindroid.androidthingsworkshop;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static final String TAG = "HomeActivity";

    private static final String LED_PIN_NAME    = "GPIO_37";
    private static final String BUTTON_PIN_NAME = "GPIO_32";


    private Gpio   ledGpio;
    private Button turnOffBtn;
    private Button turnOnBtn;

    private Gpio      buttonGpio;
    private boolean   buttonState;
    private ImageView ledImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: !!!!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ledImage = (ImageView) findViewById(R.id.led);

        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());

        try {
            // Create GPIO connection.
            buttonGpio = service.openGpio(BUTTON_PIN_NAME);

            // Configure as an input, trigger events on every change.
            buttonGpio.setDirection(Gpio.DIRECTION_IN);
            buttonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            // Value is true when the pin is LOW
            buttonGpio.setActiveType(Gpio.ACTIVE_LOW);

            buttonGpio.registerGpioCallback(buttonCallback);
        } catch (IOException e) {
            Log.w(TAG, "Error opening GPIO", e);
        }


        try {
            ledGpio = service.openGpio(LED_PIN_NAME);
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            ledGpio.setValue(false);
        } catch (IOException e) {
            Log.e(TAG, "Error opening GPIO", e);
        }
        turnOnBtn = (Button) findViewById(R.id.turn_on);
        turnOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ledGpio.setValue(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        turnOffBtn = (Button) findViewById(R.id.turn_off);
        turnOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ledGpio.setValue(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    private GpioCallback buttonCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.i(TAG, "GPIO changed, button " + gpio.getValue());
                if (gpio.getValue()){
                    buttonState = !buttonState;
                }
                if (buttonState){
                    ledImage.setImageResource(R.drawable.led_on);
                } else {
                    ledImage.setImageResource(R.drawable.led_off);
                }
            } catch (IOException e) {
                Log.w(TAG, "Error reading GPIO");
            }

            // Return true to keep callback active.
            return true;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        if (ledGpio != null){
            try {
                ledGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing GPIO", e);
            }
        }

        if (buttonGpio != null){
            buttonGpio.unregisterGpioCallback(buttonCallback);
            try {
                buttonGpio.close();
            } catch (IOException e) {
                Log.w(TAG, "Error closing GPIO", e);
            }
        }
    }
}
