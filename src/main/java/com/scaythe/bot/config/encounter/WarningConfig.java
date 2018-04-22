package com.scaythe.bot.config.encounter;

public class WarningConfig {
    private String id;
    private int offset = 0;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
