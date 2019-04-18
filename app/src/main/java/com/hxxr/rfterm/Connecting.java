package com.hxxr.rfterm;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.Display;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class Connecting extends AppCompatActivity {

    private Bundle extras;
    private ViewPropertyAnimator _flipAnim;
    private ViewPropertyAnimator _capAnim;
    private ImageView _iconR;
    private ImageView _iconSuccess;
    private ImageView _iconError;
    private ProgressBar _spin;
    private TextView _cap;
    private Delay.timer _timer;

    // This code runs as soon as the "connecting" activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // Fullscreen
        getSupportActionBar().hide(); // Remove the action bar
        setContentView(R.layout.activity_connecting);

        // Get display size
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        extras = getIntent().getExtras(); // This gets the info we sent over from MainActivity
        int[] coords = extras.getIntArray("deviceCoords"); // This is the co-ordinates of the device we clicked


        // Find all the objects on the screen
        final View dev = findViewById(R.id.device); // The device
        final TextView name = dev.findViewById(R.id.name);
        final TextView address = dev.findViewById(R.id.address);
        final TextView type = dev.findViewById(R.id.type);
        final TextView classId = dev.findViewById(R.id.classid);
        final TextView cap = findViewById(R.id.caption); // The caption below the device
        final Drawable spinner = ((ProgressBar) findViewById(R.id.spinner)).getIndeterminateDrawable(); // The spinning icon
        final ImageView icon = dev.findViewById(R.id.icon); // Icon on device
        final ImageView iconR = dev.findViewById(R.id.iconR); // Reverse side of the icon (with spinning thing)
        final ImageView iconSuccess = dev.findViewById(R.id.iconSuccess); // Icon with check mark
        final ImageView iconError = dev.findViewById(R.id.iconError); // Icon with X
        final ProgressBar spin = dev.findViewById(R.id.spinner); // Spinning thing on reverse side of icon
        final View flip = dev.findViewById(R.id.icon_flipper); // Contains both sides of the icon and the spinnning thing

        // Set the properties of the device on the screen based on data from MainActivity
        name.setText(extras.getString("deviceName"));
        address.setText(extras.getString("deviceAddress"));
        type.setText(extras.getString("deviceType"));
        classId.setText(extras.getString("deviceClassId"));
        icon.setImageResource(extras.getInt("deviceIcon"));


        dev.setY(coords[1]); // Set the co-ordinates of the device on the screen
        cap.setY(size.y*3/4); // Set the position of the caption
        spinner.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY); // Make the spinning icon white


        final ViewPropertyAnimator devAnim = dev.animate(); // This object allows us to do animations on the device object
        final ViewPropertyAnimator capAnim = cap.animate(); // And this one will animate the caption below the device object
        final ViewPropertyAnimator flipAnim = flip.animate(); // This one animates the icon AND the spinning thing

        // We need some of these views and animators later so we save them in private fields
        _flipAnim = flipAnim;
        _capAnim = capAnim;
        _iconR = iconR;
        _iconSuccess = iconSuccess;
        _iconError = iconError;
        _spin = spin;
        _cap = cap;

        final Delay.timer timer = new Delay.timer();
        _timer = timer;

        Delay.chain(new Delay.action[]{
                new Delay.action(.25) {
                    void code() {
                        devAnim.setDuration(1000); // This animation lasts 1 second
                        devAnim.setInterpolator(new AccelerateDecelerateInterpolator()); // Set interpolator
                        devAnim.y(size.y*3/10); // Move the object to a location just above the middle of the screen
                    }},
                new Delay.action(1) {
                    void code() {
                        flipAnim.setDuration(250); // This animation lasts 0.25 seconds
                        flipAnim.setInterpolator(new AccelerateDecelerateInterpolator()); // Set interpolator
                        flipAnim.scaleX(-1); // Flip the icon
                    }},
                new Delay.action(.125) { // Halfway through the flipping animation...
                    void code() {
                        icon.setVisibility(View.INVISIBLE);
                        iconR.setVisibility(View.VISIBLE);
                        spin.setVisibility(View.VISIBLE);

                        // Try to connect to the bluetooth serial port
                        Global.getInstance().connection = new RFConnect(Global.getInstance().device,
                                Global.getInstance().BT_UUID, onSuccess, onError);

                        Global.getInstance().connection.start();
                    }}
        });

        Delay.chain(new Delay.action[] {
                new Delay.action(.25) {
                    void code() {
                        capAnim.setDuration(2000); // This animation lasts 2 seconds
                        capAnim.setInterpolator(new AccelerateDecelerateInterpolator()); // Set interpolator
                        capAnim.alpha(1); // Make the caption fade in
                    }},
                new Delay.action(1) {
                    void code() {
                        Delay.repeatChain(timer, new Delay.action[]{ // Do these actions repeatedly:
                                new Delay.action(1) {
                                    void code() {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                capAnim.setDuration(1000); // This animation lasts 1 second
                                                capAnim.alpha(0.6f); // Make the caption fade out slightly
                                            }});
                                    }},
                                new Delay.action(1) {
                                    void code() {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                capAnim.alpha(1); // Make the caption fade back in
                                            }});
                                    }}});
                    }}
        });
    }

    // This code runs when we successfully connect to a device's bluetooth serial port
    // The actual BluetoothDevice we connected to is stored in Global.getInstance().device
    private Runnable onSuccess = new Runnable() {
        public void run() {
            Connecting.this.runOnUiThread(new Runnable() {
                public void run() {

                    _timer.stop(); // Stop the "breathing" animation on the caption

                    _capAnim.setDuration(1000); // This animation lasts 1 second
                    _capAnim.setInterpolator(new AccelerateDecelerateInterpolator()); // Set interpolator
                    _capAnim.alpha(0); // Make the caption fade out

                    Delay.chain(new Delay.action[]{
                            new Delay.action(1) {
                                void code() {
                                    _flipAnim.setDuration(250); // This animation lasts 0.25 seconds
                                    _flipAnim.setInterpolator(new AccelerateDecelerateInterpolator()); // Set interpolator
                                    _flipAnim.scaleX(1); // Flip the icon
                                }},
                            new Delay.action(0.125) { // Halfway through the flipping animation
                                void code() {
                                    _iconR.setVisibility(View.INVISIBLE);
                                    _spin.setVisibility(View.INVISIBLE);
                                    _iconSuccess.setVisibility(View.VISIBLE);
                                }},
                            new Delay.action(2) {
                                void code() {
                                    Intent i = new Intent(Connecting.this, Terminal.class);
                                    i.putExtra("deviceName", extras.getString("deviceName"));
                                    Connecting.this.startActivityForResult(i, 0);
                                }}
                    });

                    Delay.delay(new Delay.action(1) { // After the caption fades out
                        void code() {
                            _cap.setText(R.string.success);
                            _capAnim.alpha(1); // Make the caption fade back in
                        }});
                }});
        }
    };

    // This code runs when we fail to connect to a device's bluetooth serial port
    // The actual BluetoothDevice we connected to is stored in Global.getInstance().device
    private Runnable onError = new Runnable() {
        public void run() {
            Connecting.this.runOnUiThread(new Runnable() {
                public void run() {

                    _timer.stop(); // Stop the "breathing" animation on the caption

                    _capAnim.setDuration(1000); // This animation lasts 1 second
                    _capAnim.setInterpolator(new AccelerateDecelerateInterpolator()); // Set interpolator
                    _capAnim.alpha(0); // Make the caption fade out

                    Delay.chain(new Delay.action[]{
                            new Delay.action(1) {
                                void code() {
                                    _flipAnim.setDuration(250); // This animation lasts 0.25 seconds
                                    _flipAnim.setInterpolator(new AccelerateDecelerateInterpolator()); // Set interpolator
                                    _flipAnim.scaleX(1); // Flip the icon
                                }},
                            new Delay.action(0.125) { // Halfway through the flipping animation
                                void code() {
                                    _iconR.setVisibility(View.INVISIBLE);
                                    _spin.setVisibility(View.INVISIBLE);
                                    _iconError.setVisibility(View.VISIBLE);
                                }},
                            new Delay.action(2) { // Return back to the main screen after a while
                                void code() {
                                    setResult(Activity.RESULT_FIRST_USER);
                                    finish();
                                }}
                    });

                    Delay.delay(new Delay.action(1) { // After the caption fades out
                        void code() {
                            _cap.setText(R.string.failed_to_connect);
                            _capAnim.alpha(1); // Make the caption fade back in
                        }});
                }});
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_CANCELED) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() { } // Disallow pressing the back button
}
