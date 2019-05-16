/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.repo.sql;

import com.evolveum.midpoint.repo.api.PerformanceMonitor;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 *
 */
public class SqlPerformanceMonitorImpl implements PerformanceMonitor {

    private static final Trace LOGGER = TraceManager.getTrace(SqlPerformanceMonitorImpl.class);

    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_DETAILS = 10;

    private int level = 0;

    private AtomicLong currentHandle = new AtomicLong();

    private final ConcurrentMap<Long,OperationRecord> outstandingOperations = new ConcurrentHashMap<>();
    private final List<OperationRecord> finishedOperations = Collections.synchronizedList(new ArrayList<>());

    private SqlRepositoryFactory sqlRepositoryFactory;

    public static class SnapshotImpl implements Snapshot {
        public final Map<String, Integer> counters = new HashMap<>();       // values are never null

        private SnapshotImpl(SqlPerformanceMonitorImpl monitor) {
            counters.putAll(monitor.createCountersMap());
        }

        private SnapshotImpl(SqlPerformanceMonitorImpl monitor, Snapshot base) {
            Map<String, Integer> currentCounters = monitor.createCountersMap();
            currentCounters.forEach((kind, count) -> {
                int diff = count - base.getCounter(kind);
                if (diff > 0) {
                    counters.put(kind, diff);
                }
            });
        }

        @Override
        public int getCounter(String kind) {
            return defaultIfNull(counters.get(kind), 0);
        }

        @Override
        public Map<String, Integer> getCounters() {
            return counters;
        }
    }

    @Override
    public Snapshot createSnapshot() {
        return new SnapshotImpl(this);
    }

    @Override
    public Snapshot createDifferenceSnapshot(Snapshot base) {
        return new SnapshotImpl(this, base);
    }

    static class OperationRecord {
        String kind;
        long handle;
        int attempts;
        long startTime;
        long startCpuTime;
        long totalTime;
        long totalCpuTime;
        long wastedTime;
        long wastedCpuTime;

        public OperationRecord(String kind, long handle) {
            this.kind = kind;
            this.handle = handle;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "OperationRecord{" +
                    "kind='" + kind + '\'' +
                    ", handle=" + handle +
                    ", attempts=" + attempts +
                    ", startTime=" + new Date(startTime) +
                    ", totalTime=" + totalTime +
//                    ", totalCpuTime=" + totalCpuTime +
                    ", wastedTime=" + wastedTime +
//                    ", wastedCpuTime=" + wastedCpuTime +
                    '}';
        }
    }

    public void initialize(SqlRepositoryFactory sqlRepositoryFactory) {
        outstandingOperations.clear();
        finishedOperations.clear();
        this.sqlRepositoryFactory = sqlRepositoryFactory;
        this.level = sqlRepositoryFactory.getSqlConfiguration().getPerformanceStatisticsLevel();
        if (level >= LEVEL_NONE) {
            LOGGER.info("SQL Performance Monitor initialized (level = " + level + ").");
        }
    }

    public void shutdown() {
        if (level > LEVEL_NONE) {
            LOGGER.info("SQL Performance Monitor shutting down.");
            LOGGER.info("Statistics:\n" + getFormattedStatistics());
            String file = sqlRepositoryFactory.getSqlConfiguration().getPerformanceStatisticsFile();
            if (file != null) {
                writeStatisticsToFile(file);
            }
        }
    }

    private void writeStatisticsToFile(String file) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file, true));
            for (OperationRecord or : finishedOperations) {
                pw.println(new Date(or.startTime) + "\t" + or.kind + "\t" + or.attempts + "\t" + or.totalTime + "\t" + or.wastedTime);
            }
            for (OperationRecord or : outstandingOperations.values()) {
                pw.println(new Date(or.startTime) + "\t" + or.kind + "\t" + "?" + "\t" + "?" + "\t" + or.wastedTime);
            }
            pw.close();
            LOGGER.trace("" + (finishedOperations.size() + outstandingOperations.size()) + " record(s) written to file " + file);
        } catch (IOException e) {
            LoggingUtils.logException(LOGGER, "Couldn't write repository performance statistics to file " + file, e);
        }

    }

    static class StatEntry {
        long totalTime, wastedTime;
        int attempts;
        int records;

        public void process(OperationRecord operation) {
            totalTime += operation.totalTime;
            wastedTime += operation.wastedTime;
            attempts += operation.attempts;
            records++;
        }

        public String dump() {
            if (records == 0) {
                return "no records";
            }
            return "Records: " + records + ", " +
                   "Total time (avg/sum): " + ((float) totalTime/records) + "/" + totalTime + ", " +
                   "Wasted time (avg/sum): " + ((float) wastedTime/records) + "/" + wastedTime + " (" + (wastedTime*100.0f/totalTime) + "%), " +
                   "Attempts (avg): " + ((float) attempts/records);
        }
    }

    private String getFormattedStatistics() {
        StatEntry all = new StatEntry();
        StatEntry unfinished = new StatEntry();
        final int MAX_ATTEMPTS = SqlBaseService.LOCKING_MAX_RETRIES + 1;
        StatEntry[] perAttempts = new StatEntry[MAX_ATTEMPTS];

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            perAttempts[i] = new StatEntry();
        }

        synchronized (finishedOperations) {
            for (OperationRecord operation : finishedOperations) {
                all.process(operation);
                if (operation.attempts >= 1 && operation.attempts <= MAX_ATTEMPTS) {
                    perAttempts[operation.attempts-1].process(operation);
                } else if (operation.attempts < 0) {
                    unfinished.process(operation);
                }
            }
        }

        StringBuilder retval = new StringBuilder();
        retval.append("Overall: ").append(all.dump()).append("\n");
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            retval.append(i + 1).append(" attempt(s): ").append(perAttempts[i].dump()).append("\n");
        }
        retval.append("Unfinished: ").append(unfinished.dump()).append("\n");
        retval.append("Outstanding: ").append(outstandingOperations.toString());
        return retval.toString();
    }

    private String dump() {
        return "Finished operations: " + finishedOperations + "\nOutstanding operations: " + outstandingOperations;
    }


    public long registerOperationStart(String kind) {

        if (level <= LEVEL_NONE) {
            return 0L;
        }

        long handle = currentHandle.getAndIncrement();
        Long threadId = Thread.currentThread().getId();
        if (outstandingOperations.containsKey(threadId)) {
            OperationRecord unfinishedOperation = outstandingOperations.get(threadId);
            LOGGER.warn("Unfinished operation: " + unfinishedOperation);
            registerOperationFinishRaw(threadId, unfinishedOperation, -1);
        }
        outstandingOperations.put(threadId, new OperationRecord(kind, handle));
        return handle;
    }

    public void registerOperationFinish(long opHandle, int attempt) {

        if (level <= LEVEL_NONE) {
            return;
        }

        Long threadId = Thread.currentThread().getId();
        OperationRecord operation = outstandingOperations.get(threadId);

        if (operation == null) {
            LOGGER.warn("Attempted to record finish event for unregistered operation: handle = " + opHandle + ", attempt = " + attempt + ", ignoring the request.");
            return;
        }
        if (operation.handle != opHandle) {
            LOGGER.error("Attempted to record finish event with unexpected operation handle: handle = " + opHandle + ", stored outstanding operation for this thread = " + operation);
            outstandingOperations.remove(threadId);
            return;
        }
        registerOperationFinishRaw(threadId, operation, attempt);
    }

    private void registerOperationFinishRaw(Long threadId, OperationRecord operation, int attempt) {
        operation.totalTime = System.currentTimeMillis() - operation.startTime;
        operation.attempts = attempt;
        finishedOperations.add(operation);
        outstandingOperations.remove(threadId);
    }

    public void registerOperationNewAttempt(long opHandle, int attempt) {

        if (level <= LEVEL_NONE) {
            return;
        }

        Long threadId = Thread.currentThread().getId();
        OperationRecord operation = outstandingOperations.get(threadId);

        if (operation == null) {
            LOGGER.warn("Attempted to record new attempt event for unregistered operation: handle = " + opHandle + ", attempt = " + attempt + ", ignoring the request.");
            return;
        }
        if (operation.handle != opHandle) {
            LOGGER.error("Attempted to record new attempt event with unexpected operation handle: handle = " + opHandle + ", stored outstanding operation for this thread = " + operation);
            outstandingOperations.remove(threadId);
            return;
        }
        operation.wastedTime = System.currentTimeMillis() - operation.startTime;
        operation.attempts = attempt;
    }

    // to be used in tests
    public List<OperationRecord> getFinishedOperations(String kind) {
        List<OperationRecord> matching = new ArrayList<>();
        synchronized (finishedOperations) {
            for (OperationRecord record : finishedOperations) {
                if (Objects.equals(kind, record.kind)) {
                    matching.add(record);
                }
            }
        }
        return matching;
    }

    // to be used in tests
    public int getFinishedOperationsCount(String kind) {
        int rv = 0;
        synchronized (finishedOperations) {
            for (OperationRecord record : finishedOperations) {
                if (Objects.equals(kind, record.kind)) {
                    rv++;
                }
            }
        }
        return rv;
    }

    private Map<String, Integer> createCountersMap() {
        Map<String, Integer> rv = new HashMap<>();
        synchronized (finishedOperations) {
            for (OperationRecord record : finishedOperations) {
                rv.compute(record.kind, (kind, old) -> old == null ? 1 : old + 1);
            }
        }
        return rv;
    }


}