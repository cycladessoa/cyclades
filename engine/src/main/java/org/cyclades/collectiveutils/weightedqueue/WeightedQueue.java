/*******************************************************************************
 * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cyclades.collectiveutils.weightedqueue;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * This class is a simple wrapper to a List structure that supports a weighted
 * retrieval of the contents. The contents are not sorted. The weighted algorithm is
 * applied at the time of an attempted "get" of an item for the applicable methods.
 * 
 * XXX - This class is not thread safe! Use intrinsic lock on  all methods to make this
 * thread safe. Since this is intended to run on a a single thread...we can avoid locking
 * for now, and gain a bit of performance if any.
 *
 */
public class WeightedQueue <T> {
    /**
     * Add an item to this queue
     * 
     * @param weight    The weight to use when retrieving this item with the get or getAndRemove methods
     * @param item              The item to add to the queue
     * @throws Exception
     */
    public void add (Double weight, T item) throws Exception {
        final String eLabel = "WeightedQueue.add: ";
        if (!weightedList.add(new WeightedListEntry<T>(weight, item))) throw new Exception (eLabel + "Failed to add to list");
        scale += weight;
    }
        
    /**
     * Retrieve an item from this queue structure based on the weighted algorithm, do not remove from this queue
     * 
     * XXX - Will not remove item
     * 
     * @return The item
     */
    public T peek () {
        double probability;
        while (true) {
            probability = Math.random() * scale;
            for (WeightedListEntry<T> wle : weightedList) {
                if (probability < wle.getWeight()) return wle.getItem();
                probability -= wle.getWeight();
            }
        }
    }
        
    /**
     * Retrieve and remove an item from this queue structure based on the weighted algorithm, remove from this queue
     * 
     * XXX - Will remove item
     * 
     * @return The item
     */
    public T getAndRemove () {
        double probability;
        while (true) {
            probability = Math.random() * scale;
            WeightedListEntry<T> wle;
            for (int i = 0; i < weightedList.size(); i++) {
                wle = weightedList.get(i);
                if (probability < wle.getWeight()) { 
                    weightedList.remove(i);
                    scale -= wle.getWeight();
                    return wle.getItem();
                }
                probability -= wle.getWeight();
            }
        }
    }
        
    /**
     * Retrieve an item from this queue structure based on the index passed in
     * (based on the list implementation). 
     * 
     * XXX - Will not remove item
     * 
     * @param i The index of the item desired within this queue structure
     * @return The item
     */
    public T getItemAt (int i) {
        return weightedList.get(i).getItem();
    }
        
    /**
     * Get the size of this queue structure
     * @return  The size
     */
    public int size () {
        return weightedList.size();
    }
        
    /**
     * Sort an incoming list based the weight algorithm. List items must implement
     * interface WeightedItem.
     * 
     * @param toSort    The list of items to sort
     * @return                  The sorted list of the items passed in
     * @throws Exception
     */
    public static <T> List <WeightedItem<T>> sort (List<WeightedItem<T>> toSort) throws Exception {
        final String eLabel = "WeightedQueue.sort: ";
        try {
            List<WeightedItem<T>> returnList = new ArrayList<WeightedItem<T>>();
            WeightedQueue<WeightedItem<T>> weightedQueue = new WeightedQueue<WeightedItem<T>>();
            for (WeightedItem<T> item : toSort) {
                weightedQueue.add(item.getWeight(), item);
            }
            while (weightedQueue.size() > 0) {
                returnList.add(weightedQueue.getAndRemove());
            }
            return returnList;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
        
    /**
     * This main method will basically return the results of consecutively calling the "peek" method
     * on the arguments passed in.
     * 
     * @param args
     */
    public static void main (String[] args) {
        try {
            if (args.length < 3 || args.length % 2 != 1) {
                System.out.println("usage: cmd num_selections [unique_name weight] ...");
                System.exit(1);
            }
            WeightedQueue<String> list = new WeightedQueue<String>();
            for (int i = 1; i < args.length; i += 2) {
                list.add(Double.parseDouble(args[i + 1]), args[i]);
            }
            HashMap<String, Integer> countHash = new HashMap<String, Integer>();
            for (int i = 0; i < Integer.parseInt(args[0]); i++) {
                String c = list.peek();
                int count = 0;
                if (countHash.containsKey(c)) {
                    count = (Integer)countHash.get(c);
                }
                count++;
                countHash.put(c, count);
            }
            for (Map.Entry<String, Integer> entry : countHash.entrySet()){
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }

    private List<WeightedListEntry<T>> weightedList = new ArrayList<WeightedListEntry<T>>();
    private Double scale = new Double(0);
}

class WeightedListEntry <T> implements WeightedItem <T> {
    public WeightedListEntry (Double weight, T item) {
        this.weight = weight;
        this.item = item;
    }
    public Double getWeight () {
        return weight;
    }
    public T getItem () {
        return item;
    }
    public String toString () {
        return item.toString();
    }
    private final Double weight;
    private final T item;
}

