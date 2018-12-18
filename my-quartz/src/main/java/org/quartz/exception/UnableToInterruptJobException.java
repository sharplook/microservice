package org.quartz.exception;

public class UnableToInterruptJobException  extends SchedulerException  {

    public UnableToInterruptJobException(String msg) {
        super(msg);
    }

    public UnableToInterruptJobException(Throwable cause) {
        super(cause);
    }
}
