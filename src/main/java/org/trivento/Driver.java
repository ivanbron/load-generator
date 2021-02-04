// Copyright 2015 Ivan Brondino/Valerio Vianello
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
       
package org.trivento;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Driver {

    protected AtomicLong numberOfOperationsPerformed = new AtomicLong(0);

    /**
     * setup driver resources.
     */
    public abstract void setup();

    /**
     * create the event and send it.
     * Once the event is processed by the server increment @numberOfOperationsPerformed by one.
     *
     */
    public abstract void send(int batchSize);


    /**
     * close driver resources. i.e. close connections.
     */
    public abstract void close();

    /**
     * Returns the number of committed operations in the last interval.
     * Resets the counter to 0 for new interval.
     */
    long numOfOpsDoneInInterval(){
        return numberOfOperationsPerformed.getAndSet(0);
    }

    /**
     * For driver specific metrics, implement your own metrics reporter.
     */
    public abstract void logResults();

    /**
     * For driver specific metrics, notify a new load stage.
     * @param stageTh the received load.
     */
    public abstract void startStage(long stageTh);

    /**
     * For asynchronous drivers, implement this function to wait for pending submitted jobs
     * */
    public void waitPendingJobs() {
        //DEFAULT IMPLEMENTATION ASSUMES THAT JOBS ARE SYNCHRONOUS.
    }
}
