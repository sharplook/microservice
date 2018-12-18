
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

package org.quartz.scheduler;

import java.io.Serializable;
import java.util.Map;

public class SchedulerContext extends StringKeyDirtyFlagMap implements Serializable {
  
    private static final long serialVersionUID = -6659641334616491764L;

    public SchedulerContext() {
        super(15);
    }

    public SchedulerContext(Map<?, ?> map) {
        this();
        @SuppressWarnings("unchecked") // param must be a String key map.
		Map<String, ?> mapTyped = (Map<String, ?>)map;
        putAll(mapTyped);
    }
}
