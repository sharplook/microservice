package org.quartz.exception;

public class SchedulerException extends Exception  {

    public SchedulerException() {
        super();
    }

    public SchedulerException(String msg) {
        super(msg);
    }

    public SchedulerException(Throwable cause) {
        super(cause);
    }

    public SchedulerException(String msg, Throwable cause) {
        super(msg, cause);
    }


    /**
     * 获取详细的错误信息
     * @return
     */
    public Throwable getUnderlyingException() {
        return super.getCause();
    }

    @Override
    public String toString() {
        Throwable cause = getUnderlyingException();
        if (cause == null || cause == this) {
            return super.toString();
        } else {
            return super.toString() + " [See nested exception: " + cause + "]";
        }
    }


}
