/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
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

import opennlp.model.AbstractEventStream;
import opennlp.model.Event;

/**
 * A object which can deliver a stream of training events assuming
 * that each event is represented as a space separated list containing
 * all the contextual predicates, with the last item being the
 * outcome.
 * e.g.: 
 *
 * <p> cp_1 cp_2 ... cp_n outcome
 *
 * @author      Jason Baldridge
 * @version $Revision: 1.2 $, $Date: 2009/03/15 03:24:00 $ 
 */
public class BasicEventStream extends AbstractEventStream {
  ContextGenerator cg = new BasicContextGenerator();
  DataStream ds;
  Event next;
  
  public BasicEventStream (DataStream ds) {
    this.ds = ds;
    if (this.ds.hasNext())
      next = createEvent((String)this.ds.nextToken());
  }
  
  /**
   * Returns the next Event object held in this EventStream.  Each call to nextEvent advances the EventStream.
   *
   * @return the Event object which is next in this EventStream
   */
  public Event next () {
    while (next == null && this.ds.hasNext())
      next = createEvent((String)this.ds.nextToken());
    
    Event current = next;
    if (this.ds.hasNext()) {
      next = createEvent((String)this.ds.nextToken());
    }
    else {
      next = null;
    }
    return current;
  }
  
  /**
   * Test whether there are any Events remaining in this EventStream.
   *
   * @return true if this EventStream has more Events
   */
  public boolean hasNext () {
    while (next == null && ds.hasNext())
      next = createEvent((String)ds.nextToken());
    return next != null;
  }
  
  private Event createEvent(String obs) {
    int lastSpace = obs.lastIndexOf(' ');
    if (lastSpace == -1) 
      return null;
    else
      return new Event(obs.substring(lastSpace+1),
          cg.getContext(obs.substring(0, lastSpace)));
  }
  
  
}

