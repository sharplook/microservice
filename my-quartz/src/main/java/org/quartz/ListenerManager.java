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

import org.quartz.job.JobKey;
import org.quartz.trigger.TriggerKey;

import java.util.List;

public interface ListenerManager {


    public void addJobListener(JobListener jobListener);

    public void addJobListener(JobListener jobListener, Matcher<JobKey> matcher);

    public void addJobListener(JobListener jobListener, Matcher<JobKey>... matchers);

    public void addJobListener(JobListener jobListener, List<Matcher<JobKey>> matchers);

    public boolean addJobListenerMatcher(String listenerName, Matcher<JobKey> matcher);

    public boolean removeJobListenerMatcher(String listenerName, Matcher<JobKey> matcher);

    public boolean setJobListenerMatchers(String listenerName, List<Matcher<JobKey>> matchers);

    public List<Matcher<JobKey>> getJobListenerMatchers(String listenerName);

    public boolean removeJobListener(String name);

    public List<JobListener> getJobListeners();

    public JobListener getJobListener(String name);

    public void addTriggerListener(TriggerListener triggerListener);

    public void addTriggerListener(TriggerListener triggerListener, Matcher<TriggerKey> matcher);

    public void addTriggerListener(TriggerListener triggerListener, Matcher<TriggerKey>... matchers);

    public void addTriggerListener(TriggerListener triggerListener, List<Matcher<TriggerKey>> matchers);

    public boolean addTriggerListenerMatcher(String listenerName, Matcher<TriggerKey> matcher);

    public boolean removeTriggerListenerMatcher(String listenerName, Matcher<TriggerKey> matcher);

    public boolean setTriggerListenerMatchers(String listenerName, List<Matcher<TriggerKey>> matchers);

    public List<Matcher<TriggerKey>> getTriggerListenerMatchers(String listenerName);

    public boolean removeTriggerListener(String name);

    public List<TriggerListener> getTriggerListeners();

    public TriggerListener getTriggerListener(String name);

    public void addSchedulerListener(SchedulerListener schedulerListener);

    public boolean removeSchedulerListener(SchedulerListener schedulerListener);

    public List<SchedulerListener> getSchedulerListeners();

}