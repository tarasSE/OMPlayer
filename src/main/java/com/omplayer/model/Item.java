package com.omplayer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Item implements Comparable<Item>{
    private String url;
    private String shortName;

    @Override
    public String toString() {
        return shortName;
    }

    public static Item getInstance(final String url,final String shortName){
        Item item = new Item();
        item.setUrl(url);
        item.setShortName(shortName);
        return  item;
    }

    public static Item getInstance(final String url){
        Item item = new Item();
        item.setUrl(url);
        item.setShortName(url.substring(url.lastIndexOf("/") + 1));
        return  item;
    }

    @Override
    public int compareTo(Item o) {
        return getShortName().compareTo(o.getShortName());
    }
}
