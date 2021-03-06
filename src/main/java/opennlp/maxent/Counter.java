/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreemnets.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0 
 * (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.maxent;



/**
 * A simple class which is essentially an Integer which is mutable via
 * incrementation. 
 *
 * @author      Jason Baldridge
 * @version $Revision: 1.1 $, $Date: 2009/01/22 23:23:34 $
 */
public class Counter {
    private int counter = 1;
    public void increment() { counter++; }
    public int intValue() { return counter; }
    public boolean passesCutoff(int c) { return counter >= c; }

}
