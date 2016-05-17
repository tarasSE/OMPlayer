package com.omplayer.parser;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (!url.equals(item.url)) return false;
        return shortName.equals(item.shortName);

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + shortName.hashCode() + LocalDateTime.now().hashCode();
        return result;
    }
}
