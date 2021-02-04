// Copyright 2015 Ivan Brondino/Valerio Vianello
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package org.trivento;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LoadGenerator {

    protected static Logger log = Logger.getLogger("LoadGenerator");

    protected long targetTh;
    protected int increment;
    protected int warmUpTh;

    protected long stageTime;
    protected long warmUpTime;

    protected int batchSize;
    protected int tolerance;

    protected Driver driver;

    /**
     * @param initialTargetTh experiment starts with this target throughput
     * @param increment       each stage increments the target throughput by <b>increment</b>
     * @param warmUpTh        target throughput during warm up
     * @param stageTime       duration of each stage
     * @param warmUpTime      duration of warm up stage
     * @param batchSize       size of the batch for each send. This depends on the logic: tuples, transactions, etc.
     * @param tolerance       tolerance - each stage defines a target throughput. the tolerance defines the
     *                        percentage of target throughput allowed to be saturated.
     *                        system is saturated if actual throughput <= real throughput * (1-tolerance)/100.
     *                        expects values between 1 and 99.
     * @param driver          class which will be used to send the load, must implement @see {@link Driver}. The
     *                        system client.
     */
    public LoadGenerator(long initialTargetTh, int increment, int warmUpTh, long stageTime, long warmUpTime,
                         int batchSize, int tolerance, Driver driver) {

        this.targetTh = initialTargetTh;
        this.increment = increment;
        this.warmUpTh = warmUpTh;
        this.stageTime = stageTime;
        this.warmUpTime = warmUpTime;
        this.batchSize = batchSize;
        this.tolerance = tolerance;
        this.driver = driver;

        log.info("\nConfiguring benchmark with:" +
                 "\nexperiment starts with this target throughput." + targetTh + " ops/sec" +
                 "\neach stage increments the target throughput by " + increment + " ops/sec" +
                 "\ntarget throughput during warm up is " + warmUpTh + " ops/sec" +
                 "\nstage duration is " + stageTime + " ms" +
                 "\nwarm up duration is " + warmUpTime + " ms" +
                 "\nbatch size of each send is " + batchSize + " events" +
                 "\ntolerance is " + tolerance + "%" +
                 "\n  Ex: with a Target of 10000 events/second, system is saturated if actual throughput <= " +
                 (10000 * (100 - tolerance) / 100));
    }

    public long startBenchmark() {

        driver.setup();

        //warm up the service
        log.info("warmUp target rate: " + warmUpTh);
        long startWU = System.currentTimeMillis();

        while (System.currentTimeMillis() - startWU < warmUpTime) {
            this.send(warmUpTh);
        }

        driver.waitPendingJobs();

        driver.numOfOpsDoneInInterval();

        boolean saturated = false;
        long lastSuccTh = 0;

        while (!saturated) {

            log.info("Current target rate: " + targetTh);
            long currentTh = 0;
            long startStage = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();

            driver.startStage(targetTh);

            while (currentTime - startStage < stageTime) {
                this.send(targetTh);
                currentTime = System.currentTimeMillis();
            }

            currentTh = driver.numOfOpsDoneInInterval() * 1000 / (stageTime);
            saturated = currentTh < targetTh * (100 - tolerance) / 100;

            if (!saturated) {
                lastSuccTh = currentTh;
                log.info("Current throughput: " + lastSuccTh);
                targetTh += increment;

            }

            driver.waitPendingJobs();

        }

        driver.logResults();
        driver.close();
        log.info("SATURATED - last successful throughput: " + lastSuccTh);
        return lastSuccTh;

    }

    /**
     * For every second, sends currentTargetTh requests to the service.
     *
     * @param currentTargetTh
     */
    private void send(double currentTargetTh) {

        long opsdone = 0;
        long st = System.currentTimeMillis();

        while (opsdone < currentTargetTh) {

            driver.send(batchSize);
            opsdone += batchSize;

            while (System.currentTimeMillis() - st < ((double) opsdone) / (currentTargetTh / 1000)) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
        }
    }

}
