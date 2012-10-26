package org.gitmad.wreckroll.util;

public class CountdownTimer {
    private int countDownMS;
    private long lastPollMS;

    public CountdownTimer() {
    }
    
    public void start(int countDownMS) {
        this.countDownMS = countDownMS;
        this.lastPollMS = System.currentTimeMillis();
    }
    
    public boolean poll() {
        if (this.countDownMS != 0) {
            long curPollMS   = System.currentTimeMillis();
            this.countDownMS = (int) Math.max(0, this.countDownMS - (curPollMS - this.lastPollMS));
            this.lastPollMS  = curPollMS;
        }
        return countDownMS != 0;
    }
}