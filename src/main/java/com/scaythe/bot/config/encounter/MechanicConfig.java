package com.scaythe.bot.config.encounter;

import java.util.ArrayList;
import java.util.List;

public class MechanicConfig {
    private String id;

    private int initialDelay;
    private int period = 0;
    private int repeat = 0;

    private int duties = 0;
    private int roles = 0;
    
    private final List<WarningConfig> warnings = new ArrayList<>();

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getInitialDelay() {
        return initialDelay;
    }
    
    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }
    
    public int getPeriod() {
        return period;
    }
    
    public void setPeriod(int period) {
        this.period = period;
    }
    
    public int getRepeat() {
        return repeat;
    }
    
    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }
    
    public int getDuties() {
        return duties;
    }
    
    public void setDuties(int duties) {
        this.duties = duties;
    }
    
    public int getRoles() {
        return roles;
    }
    
    public void setRoles(int roles) {
        this.roles = roles;
    }

    public List<WarningConfig> getWarnings() {
        return warnings;
    }
}
