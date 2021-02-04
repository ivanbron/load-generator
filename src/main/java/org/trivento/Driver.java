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
