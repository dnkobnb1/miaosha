package com.miaoshaproject.dataobject;

public class Sequence {
    private String name;

    private Integer currentvalue;

    private Integer step;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getCurrentvalue() {
        return currentvalue;
    }

    public void setCurrentvalue(Integer currentvalue) {
        this.currentvalue = currentvalue;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
}