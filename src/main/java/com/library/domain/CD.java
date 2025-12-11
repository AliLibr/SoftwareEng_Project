package com.library.domain;

import com.library.strategy.CDFineStrategy;

public class CD extends LibraryItem {
    private static final long serialVersionUID = 1L;
    public static final int CD_LOAN_PERIOD_DAYS = 7;
    
    private String artist;

    public CD(String serialNumber, String title, String artist) {
        super(serialNumber, title, new CDFineStrategy());
        this.artist = artist;
    }

    public String getArtist() { return artist; }

    @Override
    public int getLoanPeriodDays() {
        return CD_LOAN_PERIOD_DAYS;
    }

    @Override
    public String toString() {
        return super.toString() + " - Artist: " + artist;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CD cd = (CD) o;
        return getId().equals(cd.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}