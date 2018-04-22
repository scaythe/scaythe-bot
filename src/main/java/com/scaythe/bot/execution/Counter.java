package com.scaythe.bot.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Counter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private int count = 0;
    
    public void increment() {
        count++;
        
        log.debug("incremented to {}", count);
    }
    
    public int get() {
        return count;
    }

    @Override
    public String toString() {
        return "Counter [count=" + count + "]";
    }
}
