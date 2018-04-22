package com.scaythe.bot.config.encounter;

import java.util.ArrayList;
import java.util.List;

public class EncounterConfig {
    private String id;
    private final List<MechanicConfig> mechanics = new ArrayList<>();
    private int duration;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public List<MechanicConfig> getMechanics() {
        return mechanics;
    }
}
