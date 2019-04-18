package com.hxxr.rfterm;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;


// Utility Class
public final class Delay {
    private Delay() { } // <- so you can't instantiate this class by using "new Delay()", and without having to make Delay an abstract class

    /**
     * Delay.action objects are passed to Delay.delay, Delay.chain or Delay.repeat as arguments.
     * For more info see the help for Delay.delay, Delay.chain and Delay.repeat.
     * Initializing Delay.action without a parameter (e.g. Delay.action()) is equivalent to Delay.action(0).
     * Example of creating a new Delay.action object (with 3 second delay):
     * <pre>
     *     new Delay.action(3) {
     *         void code() {
     *             // Your Code Here
     *         }
     *     };
     * </pre>
     * Note: Using "Override" above "void code()" is unnecessary but will not affect code
     */
    public static abstract class action {
        private double s = 0;
        private double d = 0;
        abstract void code();

        /**
         * Delay.action objects are passed to Delay.delay, Delay.chain or Delay.repeat as arguments.
         * For more info see the help for Delay.delay, Delay.chain and Delay.repeat.
         * Initializing Delay.action without a parameter (e.g. Delay.action()) is equivalent to Delay.action(0).
         * Example of creating a new Delay.action object (with 3 second delay):
         * <pre>
         *     new Delay.action(3) {
         *         void code() {
         *             // Your Code Here
         *         }
         *     };
         * </pre>
         * Note: Using "Override" above "void code()" is unnecessary but will not affect code
         * @param secs Number of seconds to delay before running code
         */
        public action(double secs) {
            s = secs;
        }
        /**
         * Delay.action objects are passed to Delay.delay, Delay.chain or Delay.repeat as arguments.
         * For more info see the help for Delay.delay, Delay.chain and Delay.repeat.
         * Initializing Delay.action without a parameter (e.g. Delay.action()) is equivalent to Delay.action(0).
         * Example of creating a new Delay.action object (with 3 second delay):
         * <pre>
         *     new Delay.action(3) {
         *         void code() {
         *             // Your Code Here
         *         }
         *     };
         * </pre>
         * Note: Using "Override" above "void code()" is unnecessary but will not affect code
         */
        public action() { }
    }

    /**
     * Delay.timer objects are passed to Delay.repeat or Delay.repeatChain as arguments.
     * For more info see the help for Delay.repeat and Delay.repeatChain.
     * Example of creating a new Delay.timer object:
     * <pre>
     *     Delay.timer timer = new Delay.timer();
     * </pre>
     */
    public static final class timer {
        private Timer t = new Timer();
        public void stop() {
            t.cancel();
            t = new Timer();
        }
        /**
         * Delay.timer objects are passed to Delay.repeat or Delay.repeatChain as arguments.
         * For more info see the help for Delay.repeat and Delay.repeatChain.
         * Example of creating a new Delay.timer object:
         * <pre>
         *     Delay.timer timer = new Delay.timer();
         * </pre>
         */
        public timer() { }
    }

    /**
     * Delays for a number of seconds before running some code.
     * Example usage (delays for 5.4 seconds and then runs some code):
     * <pre>
     *     Delay.delay(new Delay.action(5.4) {
     *         void code() {
     *             // This code will be run after 5.4 seconds
     *         }
     *     });
     * </pre>
     * Note: Using "Override" above "void code()" is unnecessary but will not affect code
     * @param action A Delay.action object containing the code to be run after delay
     */
    public static void delay(final action action) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                action.code();
            }
        }, (long)(action.s * 1000));
    }

    /**
     * Runs a sequence of code snippets one after the other with delay before each code snippet.
     * Example usage:
     * <pre>
     *     Delay.chain(new Delay.action[]{
     *         new Delay.action(0.2) {
     *             void code() {
     *                 // This code will be run after 0.2 seconds
     *             }
     *         }, new Delay.action(2.5) {
     *             void code() {
     *                 // This code will be run 2.5 seconds after the first set of code
     *             }
     *         }, new Delay.action(1) {
     *             void code() {
     *                 // This code will be run 1 second after the second set of code
     *             }
     *         }
     *     });
     * </pre>
     * Note: Using "Override" above each "void code()" is unnecessary but will not affect code
     * @param actions An array of Delay.action objects containing each code snippet
     */
    public static void chain(final action[] actions) {
        double c = 0;
        for (final action action : actions) {
            c += action.s;
            delay(new action(c) {
                void code() {
                    action.code();
                }
            });
        }
    }

    /**
     * Runs some code every few seconds.
     * Example usage (runs some code every 3.1 seconds):
     * <pre>
     *     final Delay.timer timer = new Delay.timer();
     *
     *     Delay.repeat(timer, new Delay.action(3.1) {
     *         void code() {
     *             // This code runs every 3.1 seconds
     *         }
     *     }
     *
     *     // Later we can run this to stop repeating the code:
     *     timer.stop()
     * </pre>
     * Note: Using "Override" above "void code()" is unnecessary but will not affect code
     * @param timer A Delay.timer object you can use to stop Delay.repeat at any time by running its stop method (e.g. timer.stop())
     * @param action A Delay.action object containing the code to be run at regular intervals
     */
    public static void repeat(final timer timer, final action action) {
        timer.t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                action.code();
            }
        }, (long) (action.d * 1000), (long) (action.s * 1000));
    }

    /**
     * Exactly the same as Delay.chain, except the actions are repeated endlessly in order after they are finished.
     * For more info see the help for Delay.chain.
     * Example Usage:
     * <pre>
     *     final Delay.timer timer = new Delay.timer();
     *
     *     Delay.repeatChain(timer, new Delay.action[]{
     *         new Delay.action(0.2) {
     *             void code() {
     *                 // This code will be run after 0.2 seconds
     *             }
     *         }, new Delay.action(2.5) {
     *             void code() {
     *                 // This code will be run 2.5 seconds after the first set of code
     *             }
     *         }, new Delay.action(1) {
     *             void code() {
     *                 // This code will be run 1 second after the second set of code
     *             }
     *         }
     *     });
     *
     *     // Later we can run this to stop repeating the code:
     *     timer.stop()
     * </pre>
     * Note: Using "Override" above each "void code()" is unnecessary but will not affect code
     * @param timer A Delay.timer object you can use to stop Delay.repeatChain at any time by running its stop method (e.g. timer.stop())
     * @param actions An array of Delay.action objects containing each code snippet
     */
    public static void repeatChain(final timer timer, final action[] actions) {
        double total = 0;
        double c = 0;
        for (final action action : actions) {
            total += action.s;
        }
        for (final action action : actions) {
            c += action.s;
            final action a = new action(total) {
                @Override
                void code() {
                    action.code();
                }
            };
            a.d = c;
            repeat(timer, a);
        }
    }
}