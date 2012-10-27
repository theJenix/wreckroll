package org.gitmad.wreckroll.client;

import org.gitmad.wreckroll.util.CountdownTimer;

public class BufferedClient implements WreckClient {

    private static final int countDownMS = 50;
    
    private WreckClient underlying;
    private CountdownTimer countdownTimer;

    public BufferedClient(WreckClient underlying) {
        this.underlying     = underlying;
        this.countdownTimer = new CountdownTimer();
    }

    public void forward() {
        if (!this.countdownTimer.poll()) {
            this.underlying.forward();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void stop() {
        if (!this.countdownTimer.poll()) {
            this.underlying.stop();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void reverse() {
        if (!this.countdownTimer.poll()) {
            this.underlying.reverse();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void left() {
        if (!this.countdownTimer.poll()) {
            this.underlying.left();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void right() {
        if (!this.countdownTimer.poll()) {
            this.underlying.right();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void toggleGun() {
        if (!this.countdownTimer.poll()) {
            this.underlying.toggleGun();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void toggleSmoke() {
        if (!this.countdownTimer.poll()) {
            this.underlying.toggleSmoke();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void toggleCanopy() {
        if (!this.countdownTimer.poll()) {
            this.underlying.toggleCanopy();
            this.countdownTimer.start(countDownMS);
        }
    }

    public void emergencyStop() {
        if (!this.countdownTimer.poll()) {
            this.underlying.emergencyStop();
            this.countdownTimer.start(countDownMS);
        }
    }
}
