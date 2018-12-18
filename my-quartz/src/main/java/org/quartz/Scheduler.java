
/* 
 * Copyright 2001-2009 Terracotta, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package org.quartz;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface Scheduler {
    String DEFAULT_GROUP = Key.DEFAULT_GROUP;

    String DEFAULT_RECOVERY_GROUP = "RECOVERING_JOBS";

    String DEFAULT_FAIL_OVER_GROUP = "FAILED_OVER_JOBS";

    String FAILED_JOB_ORIGINAL_TRIGGER_NAME =  "QRTZ_FAILED_JOB_ORIG_TRIGGER_NAME";

    String FAILED_JOB_ORIGINAL_TRIGGER_GROUP =  "QRTZ_FAILED_JOB_ORIG_TRIGGER_GROUP";

    String FAILED_JOB_ORIGINAL_TRIGGER_FIRETIME_IN_MILLISECONDS =  "QRTZ_FAILED_JOB_ORIG_TRIGGER_FIRETIME_IN_MILLISECONDS_AS_STRING";

    String getSchedulerName() throws SchedulerException;

    String getSchedulerInstanceId() throws SchedulerException;

    SchedulerContext getContext() throws SchedulerException;

    void start() throws SchedulerException;

    void startDelayed(int seconds) throws SchedulerException;

    boolean isStarted() throws SchedulerException;

    void standby() throws SchedulerException;

    boolean isInStandbyMode() throws SchedulerException;

    void shutdown() throws SchedulerException;

    void shutdown(boolean waitForJobsToComplete)
        throws SchedulerException;

    boolean isShutdown() throws SchedulerException;

    SchedulerMetaData getMetaData() throws SchedulerException;

    List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException;

    void setJobFactory(JobFactory factory) throws SchedulerException;
    
    ListenerManager getListenerManager()  throws SchedulerException;
    
    Date scheduleJob(JobDetail jobDetail, Trigger trigger)
        throws SchedulerException;

    Date scheduleJob(Trigger trigger) throws SchedulerException;

    void scheduleJobs(Map<JobDetail, List<Trigger>> triggersAndJobs, boolean replace) throws SchedulerException;
    
    boolean unscheduleJob(TriggerKey triggerKey)
        throws SchedulerException;

    boolean unscheduleJobs(List<TriggerKey> triggerKeys)
        throws SchedulerException;
    
    Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger)
        throws SchedulerException;
    
    void addJob(JobDetail jobDetail, boolean replace)
        throws SchedulerException;

    boolean deleteJob(JobKey jobKey)
        throws SchedulerException;

    boolean deleteJobs(List<JobKey> jobKeys)
        throws SchedulerException;
    
    void triggerJob(JobKey jobKey)
        throws SchedulerException;

    void triggerJob(JobKey jobKey, JobDataMap data)
        throws SchedulerException;

    void pauseJob(JobKey jobKey)
        throws SchedulerException;

    void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException;

    void pauseTrigger(TriggerKey triggerKey)
        throws SchedulerException;

    void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException;

    void resumeJob(JobKey jobKey)
        throws SchedulerException;

    void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException;

    void resumeTrigger(TriggerKey triggerKey)
        throws SchedulerException;

    void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException;

    void pauseAll() throws SchedulerException;

    void resumeAll() throws SchedulerException;

    List<String> getJobGroupNames() throws SchedulerException;

    Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException;

    List<? extends Trigger> getTriggersOfJob(JobKey jobKey)
        throws SchedulerException;

    List<String> getTriggerGroupNames() throws SchedulerException;

    Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException;

    Set<String> getPausedTriggerGroups() throws SchedulerException;
    
    JobDetail getJobDetail(JobKey jobKey)
        throws SchedulerException;

    Trigger getTrigger(TriggerKey triggerKey)
        throws SchedulerException;

    TriggerState getTriggerState(TriggerKey triggerKey)
        throws SchedulerException;

    void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers)
        throws SchedulerException;

    boolean deleteCalendar(String calName) throws SchedulerException;

    Calendar getCalendar(String calName) throws SchedulerException;

    List<String> getCalendarNames() throws SchedulerException;

    boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException;
    
    boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException;
    
    boolean checkExists(JobKey jobKey) throws SchedulerException;
   
    boolean checkExists(TriggerKey triggerKey) throws SchedulerException;
    
    void clear() throws SchedulerException;


}
