package com.hxxr.rfterm;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class Terminal extends AppCompatActivity implements View.OnTouchListener {
    private xtermView terminal;
    private InputMethodManager keyboard;
    private NotificationManager manager;
    private Vibrator vibrator;
    private boolean isVibrating = false;
    private View toolbar;
    private boolean isKeyboardActive = false;
    private View content = null;
    private boolean keyboardActiveBeforePause = false;
    private int ctrlState = 1;
    private ImageView ctrl;
    private int funcState = 1;
    private ImageView func;
    private int shiftState = 1;
    private ImageView shift;
    private int metaState = 1;
    private ImageView meta;
    private int colorDark = Global.getInstance().tcolorsDark[0];
    private int colorLight = Global.getInstance().tcolorsLight[0];
    private boolean isCtrlDown = false;
    private boolean isShiftDown = false;
    private boolean isMetaDown = false;
    private Bundle _extras;
    private byte r[];
    private Handler h = new Handler();
    private final Delay.timer timer = new Delay.timer();
    private AudioManager am;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove status bar
        getSupportActionBar().hide(); // Remove the action bar
        setContentView(R.layout.activity_terminal);
        final Bundle extras = getIntent().getExtras();
        _extras = extras;
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE); // Setup audio manager
        vibrator = (Vibrator)Terminal.this.getSystemService(Context.VIBRATOR_SERVICE); // Setup vibrator
        toolbar = findViewById(R.id.toolbar_wrapper);

        // Attach listener to check whether or not soft keyboard is active
        // Code was stolen from here:
        // https://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device#26964010
        content = findViewById(R.id.activity_terminal);
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                keyboardDetector.run();
            }
        });

        // Attach onClick listener to every button in the toolbar\
        findViewById(R.id.t_ctrl).setOnTouchListener(this);
        findViewById(R.id.t_esc).setOnTouchListener(this);
        findViewById(R.id.t_tab).setOnTouchListener(this);
        findViewById(R.id.t_func).setOnTouchListener(this);
        findViewById(R.id.t_ins).setOnTouchListener(this);
        findViewById(R.id.t_del).setOnTouchListener(this);
        findViewById(R.id.t_mod_more).setOnTouchListener(this);
        findViewById(R.id.t_mod_less).setOnTouchListener(this);
        findViewById(R.id.t_up).setOnTouchListener(this);
        findViewById(R.id.t_down).setOnTouchListener(this);
        findViewById(R.id.t_right).setOnTouchListener(this);
        findViewById(R.id.t_left).setOnTouchListener(this);
        findViewById(R.id.t_key1).setOnTouchListener(this);
        findViewById(R.id.t_key2).setOnTouchListener(this);
        findViewById(R.id.t_key3).setOnTouchListener(this);
        findViewById(R.id.t_key4).setOnTouchListener(this);
        findViewById(R.id.t_key5).setOnTouchListener(this);
        findViewById(R.id.t_key_more).setOnTouchListener(this);
        findViewById(R.id.t_key6).setOnTouchListener(this);
        findViewById(R.id.t_key7).setOnTouchListener(this);
        findViewById(R.id.t_key8).setOnTouchListener(this);
        findViewById(R.id.t_key9).setOnTouchListener(this);
        findViewById(R.id.t_key10).setOnTouchListener(this);
        findViewById(R.id.t_key_less).setOnTouchListener(this);
        findViewById(R.id.t_shift).setOnTouchListener(this);
        findViewById(R.id.t_meta).setOnTouchListener(this);
        findViewById(R.id.t_keyboard).setOnTouchListener(this);
        findViewById(R.id.t_exit).setOnTouchListener(this);
        findViewById(R.id.t_more).setOnTouchListener(this);

        // Setup notification
        final Intent intent = new Intent(this, Terminal.class);
        ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder binder) {
                ((NKillService.NKillBinder)binder).service.startService(new Intent(Terminal.this, NKillService.class));
                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(Terminal.this, 1, intent, 0);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(Terminal.this);
                builder.setAutoCancel(false);
                builder.setOngoing(true);
                builder.setContentIntent(pendingIntent);
                builder.setContentTitle("RFTERM Connected");
                builder.setContentText(extras.getString("deviceName"));
                builder.setSubText("RFTERM v1");
                builder.setSmallIcon(R.drawable.rf3);
                builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.rf2));
                builder.setColor(0xff03a9f4);
                builder.build();
                Notification myNotication = builder.getNotification();
                manager.notify(12345679, myNotication);
            }
            public void onServiceDisconnected(ComponentName className) { }
        };
        bindService(new Intent(Terminal.this, NKillService.class), mConnection, Context.BIND_AUTO_CREATE);

        // Setup soft keyboard
        keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        Delay.delay(new Delay.action(.25) {
            void code() {
                keyboardDetector.run();
                if (!isKeyboardActive)
                    keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });


        // Start transmitting to/receiving from bluetooth device
        Toast.makeText(Terminal.this,
                getResources().getString(R.string.terminal_connected) + " " + extras.getString("deviceName"),
                Toast.LENGTH_LONG).show();
        terminal = findViewById(R.id.terminal);
        terminal.setOnBellListener(onBell);
        terminal.setGestureListener(onGesture);
        terminal.setOnKeypadChangeListener(colorChanger);
        terminal.setOnCursorKeysChangeListener(colorChanger);
        terminal.setOnScreenBufferChangeListener(colorChanger);
        terminal.setOnMouseTrackingChangeListener(onMouseTrackingChange);
        terminal.setOnTitleChangeListener(onTitleChange);
        terminal.setTitle("RFTERM v1");
        Global.getInstance().rfio = new RFIO(
                Global.getInstance().socket,
                terminal,
                onClose,
                onLoseConnection);
        Global.getInstance().rfio.start();
    }

    // This is run repeatedly while certain keys in the toolbar are held down
    private Runnable onLongPress = new Runnable() {
        public void run() {
            Delay.repeat(timer, new Delay.action((float)ViewConfiguration.getKeyRepeatDelay()/1000) {
                void code() { output(r); }
            });
        }
    };

    // This detects onTouch events for every button in the toolbar
    @Override public boolean onTouch(View v, MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                h.removeCallbacks(onLongPress);
                timer.stop();
                break;

            case MotionEvent.ACTION_DOWN:
                v.performClick();
                switch (v.getId()) {
                    // CTRL button
                    case R.id.t_ctrl:
                        ctrl = findViewById(R.id.t_ctrl);
                        switch (ctrlState) {
                            // Switch from state 1 to state 2
                            case 1:
                                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                                ctrl.setImageResource(R.drawable.t_ctrl_2);
                                ctrlState = 2;
                                invertC();
                                break;

                            // Switch from state 2 to state 3
                            case 2:
                                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                                ctrl.setImageResource(R.drawable.t_ctrl_3);
                                ctrlState = 3;
                                break;

                            // Switch from state 3 to state 1
                            case 3:
                                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                                ctrl.setImageResource(R.drawable.t_ctrl_1);
                                ctrlState = 1;
                                invertC();
                                break;
                        }
                        break;

                    // FUNC button
                    case R.id.t_func:
                        func = findViewById(R.id.t_func);
                        switch (funcState) {
                            // Switch from state 1 to state 2
                            case 1:
                                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                                func.setImageResource(R.drawable.t_func_2);
                                funcState = 2;
                                break;

                            // Switch from state 2 to state 3
                            case 2:
                                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                                func.setImageResource(R.drawable.t_func_3);
                                funcState = 3;
                                break;

                            // Switch from state 3 to state 1
                            case 3:
                                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                                func.setImageResource(R.drawable.t_func_1);
                                funcState = 1;
                                break;
                        }
                        break;

                    // TAB button
                    case R.id.t_tab:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        output(new byte[]{9});
                        break;

                    // ESC button
                    case R.id.t_esc:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        output(new byte[]{27});
                        break;

                    // INS button
                    case R.id.t_ins:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        output(new byte[]{27, 91, 50, 126}); // ESC [ 2 ~
                        break;

                    // DEL button
                    case R.id.t_del:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        postOnLongPress(new byte[]{27, 91, 51, 126}); // ESC [ 3 ~
                        break;

                    case R.id.t_mod_more:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        findViewById(R.id.t_ctrl).setVisibility(View.GONE);
                        findViewById(R.id.t_func).setVisibility(View.GONE);
                        findViewById(R.id.t_tab).setVisibility(View.GONE);
                        findViewById(R.id.t_esc).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_ins).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_del).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_mod_more).setVisibility(View.GONE);
                        findViewById(R.id.t_mod_less).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_mod_space1).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_mod_space2).setVisibility(View.VISIBLE);
                        break;
                    case R.id.t_mod_less:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        findViewById(R.id.t_ctrl).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_func).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_tab).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_esc).setVisibility(View.GONE);
                        findViewById(R.id.t_ins).setVisibility(View.GONE);
                        findViewById(R.id.t_del).setVisibility(View.GONE);
                        findViewById(R.id.t_mod_more).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_mod_less).setVisibility(View.GONE);
                        findViewById(R.id.t_mod_space1).setVisibility(View.GONE);
                        findViewById(R.id.t_mod_space2).setVisibility(View.GONE);
                        break;

                    // Up button moves cursor upwards
                    case R.id.t_up:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        // Application Cursor Keys: send "ESC O A" to terminal
                        if (terminal.isApplicationCursorKeys()) postOnLongPress(new byte[]{27, 79, 65});
                            // Normal Cursor Keys: send "ESC [ A"
                        else postOnLongPress(new byte[]{27, 91, 65});
                        break;

                    // Down button moves cursor downwards
                    case R.id.t_down:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        // Application Cursor Keys: send "ESC O B" to terminal
                        if (terminal.isApplicationCursorKeys()) postOnLongPress(new byte[]{27, 79, 66});
                            // Normal Cursor Keys: send "ESC [ B" to terminal
                        else postOnLongPress(new byte[]{27, 91, 66});
                        break;

                    // Right button moves cursor to the right
                    case R.id.t_right:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        // Application Cursor Keys: send "ESC O C" to terminal
                        if (terminal.isApplicationCursorKeys()) postOnLongPress(new byte[]{27, 79, 67});
                            // Normal Cursor Keys: send "ESC [ C" to terminal
                        else postOnLongPress(new byte[]{27, 91, 67});
                        break;

                    // Left button moves cursor to the left
                    case R.id.t_left:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        // Application Cursor Keys: send "ESC O D" to terminal
                        if (terminal.isApplicationCursorKeys()) postOnLongPress(new byte[]{27, 79, 68});
                            // Normal Cursor Keys: send "ESC [ D" to terminal
                        else postOnLongPress(new byte[]{27, 91, 68});
                        break;

                    // If any of the six character shortcut keys are pressed, send that character to the terminal
                    case R.id.t_key1:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[0]);
                        break;
                    case R.id.t_key2:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[1]);
                        break;
                    case R.id.t_key3:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[2]);
                        break;
                    case R.id.t_key4:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[3]);
                        break;
                    case R.id.t_key5:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[4]);
                        break;
                    case R.id.t_key6:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[5]);
                        break;
                    case R.id.t_key7:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[6]);
                        break;
                    case R.id.t_key8:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[7]);
                        break;
                    case R.id.t_key9:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[8]);
                        break;
                    case R.id.t_key10:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        charProc(Global.getInstance().tshortcut[9]);
                        break;
                    case R.id.t_key_more:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        findViewById(R.id.t_key1).setVisibility(View.GONE);
                        findViewById(R.id.t_key2).setVisibility(View.GONE);
                        findViewById(R.id.t_key3).setVisibility(View.GONE);
                        findViewById(R.id.t_key4).setVisibility(View.GONE);
                        findViewById(R.id.t_key5).setVisibility(View.GONE);
                        findViewById(R.id.t_key6).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key7).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key8).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key9).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key10).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key_more).setVisibility(View.GONE);
                        findViewById(R.id.t_key_less).setVisibility(View.VISIBLE);
                        break;
                    case R.id.t_key_less:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f); // Play default tap sound
                        findViewById(R.id.t_key1).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key2).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key3).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key4).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key5).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key6).setVisibility(View.GONE);
                        findViewById(R.id.t_key7).setVisibility(View.GONE);
                        findViewById(R.id.t_key8).setVisibility(View.GONE);
                        findViewById(R.id.t_key9).setVisibility(View.GONE);
                        findViewById(R.id.t_key10).setVisibility(View.GONE);
                        findViewById(R.id.t_key_more).setVisibility(View.VISIBLE);
                        findViewById(R.id.t_key_less).setVisibility(View.GONE);
                        break;

                    // Shift button toggles shift key
                    case R.id.t_shift:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                        shift = findViewById(R.id.t_shift);
                        switch (shiftState) {
                            case 1:
                                shift.setImageResource(R.drawable.t_shift_2);
                                shiftState = 2;
                                invertS();
                                break;
                            case 2:
                                shift.setImageResource(R.drawable.t_shift_3);
                                shiftState = 3;
                                break;
                            case 3:
                                shift.setImageResource(R.drawable.t_shift_1);
                                shiftState = 1;
                                invertS();
                        }
                        break;

                    // Meta button toggles meta key
                    case R.id.t_meta:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, 0.5f); // Play space bar tap sound
                        meta = findViewById(R.id.t_meta);
                        switch (metaState) {
                            case 1:
                                meta.setImageResource(R.drawable.t_meta_2);
                                metaState = 2;
                                invertM();
                                break;
                            case 2:
                                meta.setImageResource(R.drawable.t_meta_3);
                                metaState = 3;
                                break;
                            case 3:
                                meta.setImageResource(R.drawable.t_meta_1);
                                metaState = 1;
                                invertM();
                        }
                        break;

                    // Keyboard button toggles keyboard
                    case R.id.t_keyboard:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        break;

                    // Exit button closes connection
                    case R.id.t_exit:
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.5f); // Play backspace tap sound
                        if (isKeyboardActive)
                            keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        Global.getInstance().rfio.close();
                        break;
                }
        }
        return true;
    }

    private void postOnLongPress(byte output[]) {
        for (byte b : output) {
            Log.e("TEST", String.valueOf(b));
        }
        output(output);
        r = output;
        h.postDelayed(onLongPress, ViewConfiguration.getKeyRepeatTimeout());
    }

    private void charProc(char c) {
        String string = "";
        boolean metaFlag = false;
        // If CTRL is disabled (state 1), send character normally
        if (ctrlState == 1)
            string = new String(new char[]{c});
            // If CTRL is enabled (state 2 or 3), bitwise AND the charcode with 31 before sending
        else
            string = new String(new char[]{(char)(c&31)});
        // If CTRL is in state 2, put it back into state 1
        if (ctrlState == 2) {
            ctrl.setImageResource(R.drawable.t_ctrl_1);
            ctrlState = 1;
            invertC();
        }

        // If SHIFT is enabled, turn all lowercase letters into uppercase letters
        if (shiftState > 1)
            string = string.toUpperCase();
        // If SHIFT is in state 2, put it back into state 1
        if (shiftState == 2) {
            shift.setImageResource(R.drawable.t_shift_1);
            shiftState = 1;
            invertS();
        }
        
        // If META is enabled, set meta flag to true
        if (metaState > 1)
            metaFlag = true;
        // If META is in state 2, put it back into state 1
        if (metaState == 2) {
            meta.setImageResource(R.drawable.t_meta_1);
            metaState = 1;
            invertM();
        }

        switch (c) {
            case '1':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,79,80});// If FUNC is enabled (state 2 or 3), send F1   "ESC O P"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '2':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,79,81});// If FUNC is enabled (state 2 or 3), send F2   "ESC O Q"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '3':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,79,82});// If FUNC is enabled (state 2 or 3), send F3   "ESC O R"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '4':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,79,83});// If FUNC is enabled (state 2 or 3), send F4   "ESC O S"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '5':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,49,53,126});// If FUNC is enabled (state 2 or 3), send F5   "ESC [ 1 5 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '6':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,49,55,126});// If FUNC is enabled (state 2 or 3), send F6   "ESC [ 1 7 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '7':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,49,56,126});// If FUNC is enabled (state 2 or 3), send F7   "ESC [ 1 8 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '8':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,49,57,126});// If FUNC is enabled (state 2 or 3), send F8   "ESC [ 1 9 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '9':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,50,48,126});// If FUNC is enabled (state 2 or 3), send F9   "ESC [ 2 0 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '0':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,50,49,126});// If FUNC is enabled (state 2 or 3), send F10   "ESC [ 2 1 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '-':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,50,51,126});// If FUNC is enabled (state 2 or 3), send F11   "ESC [ 2 3 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            case '=':
                if (funcState == 1) outputString(string, metaFlag); // If FUNC is disabled (state 1), send character normally
                else output(new byte[]{27,91,50,52,126});// If FUNC is enabled (state 2 or 3), send F12   "ESC [ 2 4 ~"
                if(funcState == 2){func.setImageResource(R.drawable.t_func_1);funcState=1;} // Set FUNC to state 1
                break;

            default:
                outputString(string, metaFlag);
        }
    }

    private void outputString(String s, boolean meta) {
        try {
            if (meta) output(new byte[]{27});
            Global.getInstance().rfio.send(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException f) {
            f.printStackTrace();
        }
    }
    
    private void outputString(String s) { outputString(s, false); }
    
    private void output(byte b[]) { Global.getInstance().rfio.send(b); }

    /**
     * Invert state of control key
     */
    private void invertC() {
        isCtrlDown = !isCtrlDown;
        terminal.setIsCtrlDown(isCtrlDown);
    }

    /**
     * Invert state of shift key
     */
    private void invertS() {
        isShiftDown = !isShiftDown;
        terminal.setIsShiftDown(isShiftDown);
    }

    /**
     * Invert state of meta key
     */
    private void invertM() {
        isMetaDown = !isMetaDown;
        terminal.setIsMetaDown(isMetaDown);
    }

    // This runs when title changes
    private Runnable onTitleChange = new Runnable() {
        public void run() {
            final Intent intent = new Intent(Terminal.this, Terminal.class);
            ServiceConnection mConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className, IBinder binder) {
                    ((NKillService.NKillBinder)binder).service.startService(new Intent(Terminal.this, NKillService.class));
                    manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    PendingIntent pendingIntent = PendingIntent.getActivity(Terminal.this, 1, intent, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(Terminal.this);
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    builder.setContentIntent(pendingIntent);
                    builder.setContentTitle("RFTERM Connected");
                    builder.setContentText(_extras.getString("deviceName"));
                    builder.setSubText(terminal.getTitle());
                    builder.setSmallIcon(R.drawable.rf3);
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.rf2));
                    builder.setColor(0xff03a9f4);
                    builder.build();
                    Notification myNotication = builder.getNotification();
                    manager.notify(12345679, myNotication);
                }
                public void onServiceDisconnected(ComponentName className) { }
            };
            bindService(new Intent(Terminal.this, NKillService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    };

    // This code runs when the user closes the connection
    private Runnable onClose = new Runnable() {
        public void run() {
            if (isKeyboardActive)
                keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            setResult(Activity.RESULT_OK);
            finish();
        }
    };

    // This code runs when we lose connection
    private Runnable onLoseConnection = new Runnable() {
        public void run() {
            if (isKeyboardActive)
                keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    };

    // This handles gesture events for the terminal screen
    private GestureDetector.OnGestureListener onGesture = new GestureDetector.SimpleOnGestureListener() {

        // This runs when you tap the terminal screen
        @Override public boolean onSingleTapUp(MotionEvent e) {
            // Only toggle toolbar if mouse tracking is off
            if (terminal.getMouseTrackingMode() == 0) {
                if (toolbar.getVisibility() == View.VISIBLE)
                    toolbar.setVisibility(View.GONE);
                else
                    toolbar.setVisibility(View.VISIBLE);
            }
            return true;
        }
    };

    // This code runs when a bell character is received
    private Runnable onBell = new Runnable() {
        public void run() {
            if (!isVibrating) {
                isVibrating = true;
                vibrator.vibrate(100);
                Delay.delay(new Delay.action(.15) { void code() {
                        isVibrating = false;
                    }});
            }
        }
    };

    // This code runs when the activity is paused
    @Override protected void onPause() {
        super.onPause();
        keyboardActiveBeforePause = isKeyboardActive;
        if (isKeyboardActive)
            keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    // This code runs when the activity resumes after being paused
    @Override protected void onResume() {
        super.onResume();
        Delay.delay(new Delay.action(.25) {
            void code() {
                if (isKeyboardActive != keyboardActiveBeforePause)
                    keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
    }

    // This code runs when you type something on the keyboard
    @Override public boolean onKeyUp(int keyCode, KeyEvent e) {
        switch (keyCode) {
            // If the back button is pressed, close connection
            case KeyEvent.KEYCODE_BACK:
                if (isKeyboardActive)
                    keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                Global.getInstance().rfio.close();
                return true;

            // If enter is pressed, send CR (carriage return)
            case KeyEvent.KEYCODE_ENTER:
                output(new byte[]{13});
                return true;

                // If space is pressed, send SPACE
            case KeyEvent.KEYCODE_SPACE:
                output(new byte[]{32});
                return true;

            // If shift is pressed, don't send it
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                return true;

            // If backspace is pressed, send DEL (delete)
            case KeyEvent.KEYCODE_DEL:
                output(new byte[]{127});
                return true;

            // If any other key is pressed, send it normally
            default:
                charProc((char)e.getUnicodeChar());
                return true;
        }
    }

    // This runs when the mouse tracking mode is changed
    private Runnable onMouseTrackingChange = new Runnable() {
        public void run() {
            terminal.post(new Runnable() {
                public void run() {
                    if (terminal.getMouseTrackingMode() != 0) {
                        // Show toolbar when mouse tracking is enabled since we will be unable to toggle it
                        toolbar.setVisibility(View.VISIBLE);
                        // Show message saying mouse tracking has been enabled
                        Toast.makeText(Terminal.this,
                                R.string.mouseTracking_tracking,
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                        // Show message saying mouse tracking has been disabled
                        Toast.makeText(Terminal.this,
                                R.string.mouseTracking_stopped,
                                Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    // Detects if keyboard is visible or not
    // Code was stolen from here:
    // https://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device#26964010
    private Runnable keyboardDetector = new Runnable() {
        public void run() {
            Rect r = new Rect();
            content.getWindowVisibleDisplayFrame(r);
            int screenHeight = content.getRootView().getHeight();

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            int keypadHeight = screenHeight - r.bottom;

            isKeyboardActive = keypadHeight > screenHeight * 0.15; // 0.15 ratio is perhaps enough to determine keypad height.
        }
    };

    // This is called when the terminal's keypad type, cursor keys type or screen buffer changes
    private Runnable colorChanger = new Runnable() {
        public void run() {
            terminal.post(new Runnable() {
                public void run() {
                    if (terminal.isApplicationKeypad()) {
                        if (terminal.isAlternateScreenBuffer()) {
                            if (terminal.isApplicationCursorKeys()) {
                                changeToolbarColor( // Application Keypad, Application Cursor Keys, Alternate Screen Buffer
                                        colorDark, Global.getInstance().tcolorsDark[1], colorLight, Global.getInstance().tcolorsLight[1]);
                            } else { // Application Keypad, Normal Cursor Keys, Alternate Screen Buffer
                                changeToolbarColor(
                                        colorDark, Global.getInstance().tcolorsDark[5], colorLight, Global.getInstance().tcolorsLight[5]);
                            }
                        } else {
                            if (terminal.isApplicationCursorKeys()) {
                                changeToolbarColor( // Application Keypad, Application Cursor Keys, Normal Screen Buffer
                                        colorDark, Global.getInstance().tcolorsDark[2], colorLight, Global.getInstance().tcolorsLight[2]);
                            } else { // Application Keypad, Normal Cursor Keys, Normal Screen Buffer
                                changeToolbarColor(
                                        colorDark, Global.getInstance().tcolorsDark[7], colorLight, Global.getInstance().tcolorsLight[7]);
                            }
                        }
                    } else {
                        if (terminal.isAlternateScreenBuffer()) {
                            if (terminal.isApplicationCursorKeys()) {
                                changeToolbarColor( // Normal Keypad, Application Cursor Keys, Alternate Screen Buffer
                                        colorDark, Global.getInstance().tcolorsDark[4], colorLight, Global.getInstance().tcolorsLight[4]);
                            } else { // Normal Keypad, Normal Cursor Keys, Alternate Screen Buffer
                                changeToolbarColor(
                                        colorDark, Global.getInstance().tcolorsDark[3], colorLight, Global.getInstance().tcolorsLight[3]);
                            }
                        } else {
                            if (terminal.isApplicationCursorKeys()) {
                                changeToolbarColor( // Normal Keypad, Application Cursor Keys, Normal Screen Buffer
                                        colorDark, Global.getInstance().tcolorsDark[6], colorLight, Global.getInstance().tcolorsLight[6]);
                            } else { // Normal Keypad, Normal Cursor Keys, Normal Screen Buffer
                                changeToolbarColor(
                                        colorDark, Global.getInstance().tcolorsDark[0], colorLight, Global.getInstance().tcolorsLight[0]);
                            }
                        }
                    }
                }
            });
        }
    };

    private void changeToolbarColor(int darkFrom, int darkTo, int lightFrom, int lightTo) {
        final float[] dfrom = new float[3],
                dto =   new float[3],
                lfrom = new float[3],
                lto = new float[3],
                dhsv = new float[3],
                lhsv = new float[3];

        Color.colorToHSV(darkFrom, dfrom);
        Color.colorToHSV(darkTo, dto);
        Color.colorToHSV(lightFrom, lfrom);
        Color.colorToHSV(lightTo, lto);

        final ValueAnimator danim = ValueAnimator.ofFloat(0, 1),
                lanim = ValueAnimator.ofFloat(0, 1);
        danim.setDuration(450);
        lanim.setDuration(450);

        danim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                dhsv[0] = dfrom[0] + (dto[0] - dfrom[0])*animation.getAnimatedFraction();
                dhsv[1] = dfrom[1] + (dto[1] - dfrom[1])*animation.getAnimatedFraction();
                dhsv[2] = dfrom[2] + (dto[2] - dfrom[2])*animation.getAnimatedFraction();
                colorDark = Color.HSVToColor(dhsv);
                findViewById(R.id.toolbar_wrapper).setBackgroundColor(colorDark);
                ((ImageView)findViewById(R.id.t_chevron1)).setColorFilter(colorDark);
                findViewById(R.id.t_chevron2).setBackgroundColor(colorDark);
                ((ImageView)findViewById(R.id.t_up)).setColorFilter(colorDark);
                ((ImageView)findViewById(R.id.t_down)).setColorFilter(colorDark);
                ((ImageView)findViewById(R.id.t_right)).setColorFilter(colorDark);
                ((ImageView)findViewById(R.id.t_left)).setColorFilter(colorDark);
                ((TextView)findViewById(R.id.t_key1)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key2)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key3)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key4)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key5)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key6)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key7)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key8)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key9)).setTextColor(colorDark);
                ((TextView)findViewById(R.id.t_key10)).setTextColor(colorDark);
                ((ImageView)findViewById(R.id.t_key_more)).setColorFilter(colorDark);
                ((ImageView)findViewById(R.id.t_key_less)).setColorFilter(colorDark);
            }
        });
        lanim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                lhsv[0] = lfrom[0] + (lto[0] - lfrom[0])*animation.getAnimatedFraction();
                lhsv[1] = lfrom[1] + (lto[1] - lfrom[1])*animation.getAnimatedFraction();
                lhsv[2] = lfrom[2] + (lto[2] - lfrom[2])*animation.getAnimatedFraction();
                colorLight = Color.HSVToColor(lhsv);
                ((ImageView)findViewById(R.id.t_ctrl)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_esc)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_tab)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_func)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_ins)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_del)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_mod_more)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_mod_less)).setColorFilter(colorLight);
                findViewById(R.id.t_chevron1).setBackgroundColor(colorLight);
                findViewById(R.id.t_up).setBackgroundColor(colorLight);
                findViewById(R.id.t_down).setBackgroundColor(colorLight);
                findViewById(R.id.t_right).setBackgroundColor(colorLight);
                findViewById(R.id.t_left).setBackgroundColor(colorLight);
                findViewById(R.id.t_keyspacer).setBackgroundColor(colorLight);
                findViewById(R.id.t_key1).setBackgroundColor(colorLight);
                findViewById(R.id.t_key2).setBackgroundColor(colorLight);
                findViewById(R.id.t_key3).setBackgroundColor(colorLight);
                findViewById(R.id.t_key4).setBackgroundColor(colorLight);
                findViewById(R.id.t_key5).setBackgroundColor(colorLight);
                findViewById(R.id.t_key6).setBackgroundColor(colorLight);
                findViewById(R.id.t_key7).setBackgroundColor(colorLight);
                findViewById(R.id.t_key8).setBackgroundColor(colorLight);
                findViewById(R.id.t_key9).setBackgroundColor(colorLight);
                findViewById(R.id.t_key10).setBackgroundColor(colorLight);
                findViewById(R.id.t_key_more).setBackgroundColor(colorLight);
                findViewById(R.id.t_key_less).setBackgroundColor(colorLight);
                ((ImageView)findViewById(R.id.t_chevron2)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_shift)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_meta)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_keyboard)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_exit)).setColorFilter(colorLight);
                ((ImageView)findViewById(R.id.t_more)).setColorFilter(colorLight);
            }
        });

        terminal.post(new Runnable() {
            @Override public void run() {
                danim.start();
                lanim.start();
            }
        });

    }

    // This is called when back button is pressed
    @Override public void onBackPressed() { }

    @Override protected void onDestroy() {
        super.onDestroy();
        Global.getInstance().rfio.close();
        manager.cancel(12345679);
    }
}
