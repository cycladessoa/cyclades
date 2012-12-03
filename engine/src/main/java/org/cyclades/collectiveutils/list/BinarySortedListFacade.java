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
package org.cyclades.collectiveutils.list;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class BinarySortedListFacade <T extends Comparable<T>> implements Iterable<T> {
    /**
     * Default constructor that uses an ArrayList implementation
     */
    public BinarySortedListFacade () {
        list = new ArrayList<T>();
    }

    /**
     * Constructor that takes a List implementation. If the List passed in contains more than
     * one element...it is sorted using the Binary Sort algorithm below.
     *
     * @param list
     * @throws NullPointerException if the list passed in is null
     */
    public BinarySortedListFacade (List<T> list) throws NullPointerException {
        final String eLabel = "BinarySortedListFacade.BinarySortListFacade: ";
        if (list == null) throw new NullPointerException(eLabel + "List parameter cannot be null");
        this.list = list;
        if (list.size() > 1) binarySort(list);
    }

    /**
     * Add an item to this data structure
     *
     * @param item
     */
    public void add (T item) {
        binaryInsertionSort(item, list);
    }

    /**
     * Get the iterator of the underlying list implementation
     *
     * @return an Iterator
     */
    public Iterator<T>iterator () {
        return list.iterator();
    }

    /**
     * Sort the list passed in using a binary sorting algorithm. Sorts in place.
     *
     * @param <T>
     * @param list
     */
    public static <T extends Comparable<? super T>> void binarySort (List<T> list) {
        final String eLabel = "BinarySortedListFacade.binarySort: ";
        if (list == null) throw new NullPointerException(eLabel + "List parameter must not be null");
        T value;
        int left;
        int right;
        int mid;
        for (int i = 0; i < list.size(); i++) {
            value = list.get(i);
            left = 0;
            right = i;
            while (left <= right) {
                mid = (left + right) / 2;
                if (value.compareTo(list.get(mid)) >= 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
            if (i > left) list.add(left, list.remove(i));
        }
    }

    /**
     * Add an item to an already sorted List. The list passed in must be sorted!
     *
     * @param item      The item to add to the list parameter
     * @param list      THe List to add the item parameter to. This must be sorted prior to calling this method.
     */
    public static <T extends Comparable<? super T>> void binaryInsertionSort (T item, List<T> list) {
        final String eLabel = "BinarySortedListFacade.binaryInsertionSort: ";
        if (item == null) throw new NullPointerException(eLabel + "<T>item parameter must not be null");
        if (list == null) throw new NullPointerException(eLabel + "List parameter must not be null");
        int left = 0;
        int right = list.size() - 1;
        int mid;
        while (left <= right) {
            mid = (left + right) / 2;
            if (item.compareTo(list.get(mid)) >= 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        list.add(left, item);
    }

    public String toString () {
        return list.toString();
    }

    private List<T> list;
}
