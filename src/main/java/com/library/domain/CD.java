package com.library.domain;

import com.library.strategy.CDFineStrategy;

/**
 * Represents a CD entity.
 */
public class CD extends LibraryItem {
    private String artist;

    public CD(String serialNumber, String title, String artist) {
        super(serialNumber, title, new CDFineStrategy());
        this.artist = artist;
    }

    public String getArtist() { return artist; }

    @Override
    public int getLoanPeriodDays() {
        return 7;
    }

    @Override
    public String toString() {
        return super.toString() + " - Artist: " + artist;
    }
}