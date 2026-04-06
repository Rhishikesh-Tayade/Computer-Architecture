package processor.memorysystem;

import java.util.HashMap;
import java.util.Map;

import configuration.Configuration;
import generic.Element;
import generic.Event;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.MemoryWriteEvent;
import generic.Simulator;
import processor.Clock;
import processor.Processor;

/**
 * L1 Cache (Instruction or Data).
 * - 2-way set-associative, LRU replacement.
 * - Write-through policy.
 * - Line size = 4 bytes (one word per line, matching ISA word width).
 * 
 * Event flow for reads (cache hit):
 * Pipeline → MemoryReadEvent → Cache.handleEvent → Cache.cacheRead →
 * MemoryResponseEvent(latency) → Pipeline
 *
 * Event flow for reads (cache miss):
 * Pipeline → MemoryReadEvent → Cache.handleEvent → Cache.cacheRead →
 * MemoryReadEvent(latency) → MainMemory
 * MainMemory → MemoryResponseEvent(now) → Cache.handleEvent →
 * handleMissResponse → MemoryResponseEvent(now) → Pipeline
 *
 * Event flow for writes (cache hit):
 * Pipeline → MemoryWriteEvent → Cache.handleEvent → Cache.cacheWrite →
 * MemoryWriteEvent(latency) → MainMemory
 * (in parallel) MemoryResponseEvent(latency) → Pipeline
 *
 * Event flow for writes (cache miss):
 * Pipeline → MemoryWriteEvent → Cache.handleEvent → Cache.cacheWrite →
 * (Fetch line) MemoryReadEvent(latency) → MainMemory
 * MainMemory → MemoryResponseEvent(now) → Cache.handleEvent →
 * handleMissResponse →
 * (Write-through) MemoryWriteEvent(now) → MainMemory
 * (Notify pipeline) MemoryResponseEvent(now) → Pipeline
 */
public class Cache implements Element {
    Processor containingProcessor;
    int latency;
    int numberOfSets;
    int associativity;
    CacheLine[][] sets;

    // When cache size is 0, this cache behaves as a bypass (used for "No Cache").
    boolean noCache;

    // Pending write miss: address → (value, requester)
    Map<Integer, Integer> pendingWriteValues;
    Map<Integer, Element> pendingWriteRequestors;
    // Pending read miss: address → requester
    Map<Integer, Element> pendingReadRequestors;

    public Cache(Processor containingProcessor, int cacheSize, int latency, int associativity) {
        this.containingProcessor = containingProcessor;
        this.latency = latency;
        this.associativity = associativity;
        int numberOfLines = cacheSize / 4; // 4 bytes per word
        this.noCache = (numberOfLines <= 0);

        if (noCache) {
            this.numberOfSets = 0;
            this.sets = new CacheLine[0][associativity];
        } else {
            this.numberOfSets = Math.max(1, numberOfLines / associativity);

            this.sets = new CacheLine[numberOfSets][associativity];
            for (int i = 0; i < numberOfSets; i++) {
                for (int j = 0; j < associativity; j++) {
                    sets[i][j] = new CacheLine();
                }
            }
        }

        this.pendingReadRequestors = new HashMap<>();
        this.pendingWriteValues = new HashMap<>();
        this.pendingWriteRequestors = new HashMap<>();
    }

    // ----------------------- Cache lookup helpers -----------------------

    private int setIndex(int address) {
        return address % numberOfSets;
    }

    private int tag(int address) {
        return address / numberOfSets;
    }

    /** Returns the way index if hit, -1 if miss. */
    private int findWay(int setIdx, int tag) {
        for (int i = 0; i < associativity; i++) {
            CacheLine line = sets[setIdx][i];
            if (line.isValid() && line.getTag() == tag) {
                return i;
            }
        }
        return -1;
    }

    /** Returns LRU way index in the given set. */
    private int lruWay(int setIdx) {
        int lru = 0;
        long minTime = Long.MAX_VALUE;
        for (int i = 0; i < associativity; i++) {
            if (!sets[setIdx][i].isValid())
                return i; // empty slot
            if (sets[setIdx][i].getLastUsedTime() < minTime) {
                minTime = sets[setIdx][i].getLastUsedTime();
                lru = i;
            }
        }
        return lru;
    }

    // ----------------------- Cache operations -----------------------

    /**
     * Called by handleEvent when pipeline issues a read (MemoryReadEvent).
     */
    private void cacheRead(int address, Element requestor) {
        if (noCache) {
            // Bypass: always fetch from main memory with main-memory latency.
            pendingReadRequestors.put(address, requestor);
            Simulator.getEventQueue().addEvent(
                    new MemoryReadEvent(
                            Clock.getCurrentTime() + Configuration.mainMemoryLatency,
                            this,
                            containingProcessor.getMainMemory(),
                            address));
            return;
        }

        int setIdx = setIndex(address);
        int tg = tag(address);
        int way = findWay(setIdx, tg);

        if (way >= 0) {
            // Cache HIT
            sets[setIdx][way].setLastUsedTime(Clock.getCurrentTime());
            int data = sets[setIdx][way].getData();
            // Respond to pipeline after cache latency
            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            Clock.getCurrentTime() + latency,
                            this,
                            requestor,
                            data,
                            address));
        } else {
            // Cache MISS — fetch from main memory
            pendingReadRequestors.put(address, requestor);
            Simulator.getEventQueue().addEvent(
                    new MemoryReadEvent(
                            Clock.getCurrentTime() + latency + Configuration.mainMemoryLatency, // miss penalty
                            this, // response will come back to cache
                            containingProcessor.getMainMemory(),
                            address));
        }
    }

    /**
     * Called by handleEvent when pipeline issues a write (MemoryWriteEvent).
     * Write-Through: always write to main memory; update cache if present.
     */
    private void cacheWrite(int address, int value, Element requestor) {
        if (noCache) {
            // Bypass: always write to main memory and complete after main-memory latency.
            long doneTime = Clock.getCurrentTime() + Configuration.mainMemoryLatency;
            Simulator.getEventQueue().addEvent(
                    new MemoryWriteEvent(
                            doneTime,
                            this,
                            containingProcessor.getMainMemory(),
                            address,
                            value));
            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            doneTime,
                            this,
                            requestor,
                            0,
                            address));
            return;
        }

        int setIdx = setIndex(address);
        int tg = tag(address);
        int way = findWay(setIdx, tg);

        if (way >= 0) {
            // Cache HIT on write — update cache line and write through
            sets[setIdx][way].setData(value);
            sets[setIdx][way].setLastUsedTime(Clock.getCurrentTime());

            // Write-through: send write to MainMemory (not back to cache!)
            long doneTime = Clock.getCurrentTime() + latency + Configuration.mainMemoryLatency;
            Simulator.getEventQueue().addEvent(
                    new MemoryWriteEvent(
                            doneTime,
                            this,
                            containingProcessor.getMainMemory(), // target is main memory
                            address,
                            value));

            // Inform pipeline that write is complete (in parallel with MM write)
            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            doneTime,
                            this,
                            requestor,
                            0,
                            address));
        } else {
            // Cache MISS on write:
            // Since line size is 1 word (4B), we don't need to read the old value.
            // Allocate the line with the new write value, then write-through to main memory.
            int missWay = lruWay(setIdx);
            CacheLine line = sets[setIdx][missWay];
            line.setValid(true);
            line.setTag(tg);
            line.setData(value);
            line.setLastUsedTime(Clock.getCurrentTime());

            long doneTime = Clock.getCurrentTime() + latency + Configuration.mainMemoryLatency;
            Simulator.getEventQueue().addEvent(
                    new MemoryWriteEvent(
                            doneTime,
                            this,
                            containingProcessor.getMainMemory(),
                            address,
                            value));

            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            doneTime,
                            this,
                            requestor,
                            0,
                            address));
        }
    }

    /**
     * Called when MainMemory responds with the fetched cache line.
     * Handles both read-miss and write-miss responses.
     */
    private void handleMissResponse(int address, int value) {
        if (noCache) {
            // Only reads should use this in bypass mode.
            if (pendingReadRequestors.containsKey(address)) {
                Element requestor = pendingReadRequestors.remove(address);
                Simulator.getEventQueue().addEvent(
                        new MemoryResponseEvent(
                                Clock.getCurrentTime(),
                                this,
                                requestor,
                                value,
                                address));
            }
            return;
        }

        int setIdx = setIndex(address);
        int tg = tag(address);
        int way = lruWay(setIdx);

        // Install the fetched line
        CacheLine line = sets[setIdx][way];
        line.setValid(true);
        line.setTag(tg);
        line.setData(value);
        line.setLastUsedTime(Clock.getCurrentTime());

        if (pendingReadRequestors.containsKey(address)) {
            // Read miss: return fetched value to pipeline
            Element requestor = pendingReadRequestors.remove(address);
            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            Clock.getCurrentTime(),
                            this,
                            requestor,
                            value,
                            address));

        } else if (pendingWriteRequestors.containsKey(address)) {
            // Write miss: write new value to cache and main memory
            int writeValue = pendingWriteValues.remove(address);
            Element requestor = pendingWriteRequestors.remove(address);

            line.setData(writeValue);

            // Write-through to main memory
            Simulator.getEventQueue().addEvent(
                    new MemoryWriteEvent(
                            Clock.getCurrentTime() + Configuration.mainMemoryLatency,
                            this,
                            containingProcessor.getMainMemory(),
                            address,
                            writeValue));

            // Inform pipeline that write is done
            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            Clock.getCurrentTime() + Configuration.mainMemoryLatency,
                            this,
                            requestor,
                            0,
                            address));
        }
    }

    // ----------------------- Element interface -----------------------

    @Override
    public void handleEvent(Event e) {
        if (e.getEventType() == Event.EventType.MemoryRead) {
            // Pipeline is requesting a read
            MemoryReadEvent event = (MemoryReadEvent) e;
            cacheRead(event.getAddressToReadFrom(), event.getRequestingElement());

        } else if (e.getEventType() == Event.EventType.MemoryWrite) {
            // Pipeline is requesting a write
            MemoryWriteEvent event = (MemoryWriteEvent) e;
            cacheWrite(event.getAddressToWriteTo(), event.getValueToWrite(), event.getRequestingElement());

        } else if (e.getEventType() == Event.EventType.MemoryResponse) {
            // MainMemory responded to our fetch request (miss handling)
            MemoryResponseEvent event = (MemoryResponseEvent) e;
            handleMissResponse(event.getAddress(), event.getValue());
        }
    }
}
