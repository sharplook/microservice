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

package org.quartz.job;

public class JobBuilder {

    private JobKey key;
    private String description;
    private Class<? extends Job> jobClass = NoOpJob.class;
    private boolean durability;
    private boolean shouldRecover;
    
    private JobDataMap jobDataMap = new JobDataMap();
    
    private JobBuilder() {
    }

    public static JobBuilder newJob() {
        return new JobBuilder();
    }

    public static JobBuilder newJob(Class <? extends Job> jobClass) {
        JobBuilder b = new JobBuilder();
        b.ofType(jobClass);
        return b;
    }

    public JobDetail build() {

        JobDetailImpl job = new JobDetailImpl();
        
        job.setJobClass(jobClass);
        job.setDescription(description);
        if(key == null)
            key = new JobKey(Key.createUniqueName(null), null);
        job.setKey(key); 
        job.setDurability(durability);
        job.setRequestsRecovery(shouldRecover);
        
        
        if(!jobDataMap.isEmpty())
            job.setJobDataMap(jobDataMap);
        
        return job;
    }

    public JobBuilder withIdentity(String name) {
        key = new JobKey(name, null);
        return this;
    }  

    public JobBuilder withIdentity(String name, String group) {
        key = new JobKey(name, group);
        return this;
    }
    

    public JobBuilder withIdentity(JobKey jobKey) {
        this.key = jobKey;
        return this;
    }
    

    public JobBuilder withDescription(String jobDescription) {
        this.description = jobDescription;
        return this;
    }

    public JobBuilder ofType(Class <? extends Job> jobClazz) {
        this.jobClass = jobClazz;
        return this;
    }


    public JobBuilder requestRecovery() {
        this.shouldRecover = true;
        return this;
    }


    public JobBuilder requestRecovery(boolean jobShouldRecover) {
        this.shouldRecover = jobShouldRecover;
        return this;
    }

    public JobBuilder storeDurably() {
        this.durability = true;
        return this;
    }
    

    public JobBuilder storeDurably(boolean jobDurability) {
        this.durability = jobDurability;
        return this;
    }

    public JobBuilder usingJobData(String dataKey, String value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    

    public JobBuilder usingJobData(String dataKey, Integer value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    

    public JobBuilder usingJobData(String dataKey, Long value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    

    public JobBuilder usingJobData(String dataKey, Float value) {
        jobDataMap.put(dataKey, value);
        return this;
    }
    

    public JobBuilder usingJobData(String dataKey, Double value) {
        jobDataMap.put(dataKey, value);
        return this;
    }

    public JobBuilder usingJobData(String dataKey, Boolean value) {
        jobDataMap.put(dataKey, value);
        return this;
    }

    public JobBuilder usingJobData(JobDataMap newJobDataMap) {
        // add any existing data to this new map
        for(String dataKey: jobDataMap.keySet()) {
            newJobDataMap.put(dataKey, jobDataMap.get(dataKey));
        }
        jobDataMap = newJobDataMap; // set new map as the map to use
        return this;
    }
    
}
