///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2003 Thomas Morton
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////   
package opennlp.maxent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import gnu.trove.*;

/** 
 * @author Tom Morton
 *
 */
public class TwoPassDataIndexer extends AbstractDataIndexer{

  /**
   * One argument constructor for DataIndexer which calls the two argument
   * constructor assuming no cutoff.
   *
   * @param events An Event[] which contains the a list of all the Events
   *               seen in the training data.
   */
  public TwoPassDataIndexer(EventStream eventStream1) {
    this(eventStream1, 0);
  }

  /**
       * Two argument constructor for DataIndexer.
       *
       * @param events An Event[] which contains the a list of all the Events
       *               seen in the training data.
       * @param cutoff The minimum number of times a predicate must have been
       *               observed in order to be included in the model.
       */
  public TwoPassDataIndexer(EventStream eventStream, int cutoff) {
    TObjectIntHashMap predicateIndex;
    TLinkedList events;
    List eventsToCompare;

    predicateIndex = new TObjectIntHashMap();
    System.out.println("Indexing events using cutoff of " + cutoff + "\n");

    System.out.print("\tComputing event counts...  ");
    try {
      File tmp = File.createTempFile("events", null);
      tmp.deleteOnExit();
      int numEvents = computeEventCounts(eventStream, new FileWriter(tmp), predicateIndex, cutoff);
      System.out.println("done. " + numEvents + " events");

      System.out.print("\tIndexing...  ");

      eventsToCompare = index(numEvents, new FileEventStream(tmp), predicateIndex);
      // done with event list
      events = null;
      // done with predicates
      predicateIndex = null;
      tmp.delete();
      System.out.println("done.");

      System.out.print("Sorting and merging events... ");
      sortAndMerge(eventsToCompare);
      System.out.println("Done indexing.");
    }
    catch(IOException e) {
      System.err.println(e);
    }
  }

  /**
      * Reads events from <tt>eventStream</tt> into a linked list.  The
      * predicates associated with each event are counted and any which
      * occur at least <tt>cutoff</tt> times are added to the
      * <tt>predicatesInOut</tt> map along with a unique integer index.
      *
      * @param eventStream an <code>EventStream</code> value
      * @param eventStore a writer to which the events are written to for later processing.
      * @param predicatesInOut a <code>TObjectIntHashMap</code> value
      * @param cutoff an <code>int</code> value
      */
  private int computeEventCounts(EventStream eventStream, Writer eventStore, TObjectIntHashMap predicatesInOut, int cutoff) throws IOException {
    TObjectIntHashMap counter = new TObjectIntHashMap();
    int predicateIndex = 0;
    int eventCount = 0;
    while (eventStream.hasNext()) {
      Event ev = eventStream.nextEvent();
      eventCount++;
      eventStore.write(FileEventStream.toLine(ev));
      String[] ec = ev.getContext();
      for (int j = 0; j < ec.length; j++) {
        if (!predicatesInOut.containsKey(ec[j])) {
          if (counter.increment(ec[j])) {}
          else {
            counter.put(ec[j], 1);
          }
          if (counter.get(ec[j]) >= cutoff) {
            predicatesInOut.put(ec[j], predicateIndex++);
            counter.remove(ec[j]);
          }
        }
      }
    }
    predicatesInOut.trimToSize();
    eventStore.close();
    return eventCount;
  }

  private List index(int numEvents, EventStream es, TObjectIntHashMap predicateIndex) {
    TObjectIntHashMap omap = new TObjectIntHashMap();
    int outcomeCount = 0;
    int predCount = 0;
    List eventsToCompare = new ArrayList(numEvents);
    TIntArrayList indexedContext = new TIntArrayList();
    while (es.hasNext()) {
      Event ev = es.nextEvent();
      String[] econtext = ev.getContext();
      ComparableEvent ce;

      int predID, ocID;
      String oc = ev.getOutcome();

      if (omap.containsKey(oc)) {
        ocID = omap.get(oc);
      }
      else {
        ocID = outcomeCount++;
        omap.put(oc, ocID);
      }

      for (int i = 0; i < econtext.length; i++) {
        String pred = econtext[i];
        if (predicateIndex.containsKey(pred)) {
          indexedContext.add(predicateIndex.get(pred));
        }
      }

      // drop events with no active features
      if (indexedContext.size() > 0) {
        ce = new ComparableEvent(ocID, indexedContext.toNativeArray());
        eventsToCompare.add(ce);
      }
      else {
        System.err.println("Dropped event " + ev.getOutcome() + ":" + Arrays.asList(ev.getContext()));
      }
      // recycle the TIntArrayList
      indexedContext.resetQuick();
    }
    outcomeLabels = toIndexedStringArray(omap);
    predLabels = toIndexedStringArray(predicateIndex);
    return eventsToCompare;
  }

}

class FileEventStream implements EventStream {

  BufferedReader reader;
  String line;

  public FileEventStream(String fileName) throws IOException {
    reader = new BufferedReader(new FileReader(fileName));
  }

  public FileEventStream(File file) throws IOException {
    reader = new BufferedReader(new FileReader(file));
  }

  public boolean hasNext() {
    try {
      return (null != (line = reader.readLine()));
    }
    catch (IOException e) {
      System.err.println(e);
      return (false);
    }
  }

  public Event nextEvent() {
    StringTokenizer st = new StringTokenizer(line);
    String outcome = st.nextToken();
    int count = st.countTokens();
    String[] context = new String[count];
    for (int ci = 0; ci < count; ci++) {
      context[ci] = st.nextToken();
    }
    return (new Event(outcome, context));
  }
  
  public static String toLine(Event e) {
    StringBuffer sb = new StringBuffer();
    sb.append(e.getOutcome());
    String[] context = e.getContext();
    for (int ci=0,cl=context.length;ci<cl;ci++) {
      sb.append(" "+context[ci]);
    }
    sb.append(System.getProperty("line.separator"));
    return sb.toString();
  }
}