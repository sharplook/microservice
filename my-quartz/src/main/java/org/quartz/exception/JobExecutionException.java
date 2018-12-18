package org.quartz.exception;

public class JobExecutionException extends SchedulerException {


    //是否立即重新触发JOB
    private boolean refire = false;

    //job会被多个触发器调用,如果发现是当前触发器的问题,,则true取消当前特定触发器的调用
    private boolean unscheduleTrigg = false;

    //job会被多个触发器调用,如果发现是当前JOB的问题,
    //则自动取消所有与当前job相关的trigger的调度,使其不再运行.
    private boolean unscheduleAllTriggs = false;


    public JobExecutionException() {
    }

    public JobExecutionException(Throwable cause) {
        super(cause);
    }

    public JobExecutionException(String msg) {
        super(msg);
    }

    public JobExecutionException(boolean refireImmediately) {
        refire = refireImmediately;
    }

    public JobExecutionException(Throwable cause, boolean refireImmediately) {
        super(cause);
        refire = refireImmediately;
    }

    public JobExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public JobExecutionException(String msg, Throwable cause,boolean refireImmediately) {
        super(msg, cause);
        refire = refireImmediately;
    }

    public JobExecutionException(String msg, boolean refireImmediately) {
        super(msg);

        refire = refireImmediately;
    }


    public void setRefireImmediately(boolean refire) {
        this.refire = refire;
    }

    public boolean refireImmediately() {
        return refire;
    }

    public void setUnscheduleFiringTrigger(boolean unscheduleTrigg) {
        this.unscheduleTrigg = unscheduleTrigg;
    }

    public boolean unscheduleFiringTrigger() {
        return unscheduleTrigg;
    }

    public void setUnscheduleAllTriggers(boolean unscheduleAllTriggs) {
        this.unscheduleAllTriggs = unscheduleAllTriggs;
    }

    public boolean unscheduleAllTriggers() {
        return unscheduleAllTriggs;
    }
}
