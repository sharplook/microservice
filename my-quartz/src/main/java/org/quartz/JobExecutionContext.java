/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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

import org.quartz.job.JobDataMap;
import org.quartz.job.JobDetail;

import java.util.Date;

public interface JobExecutionContext {

    /**
     * d当前执行计划
     * @return
     */
    public Scheduler getScheduler();

    /**
     * 当前触发器
     * @return
     */
    public Trigger getTrigger();

    public Calendar getCalendar();

    public boolean isRecovering();

    public int getRefireCount();

    public JobDataMap getMergedJobDataMap();

    public JobDetail getJobDetail();

    public Job getJobInstance();

    public Date getFireTime();

    public Date getScheduledFireTime();

    public Date getPreviousFireTime();

    public Date getNextFireTime();

    public String getFireInstanceId();

    public Object getResult();

    public void setResult(Object result);

    public long getJobRunTime();

    public void put(Object key, Object value);

    public Object get(Object key);

}