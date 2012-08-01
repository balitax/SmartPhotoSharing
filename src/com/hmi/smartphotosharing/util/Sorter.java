package com.hmi.smartphotosharing.util;

import java.util.Comparator;

import com.hmi.smartphotosharing.json.Group;

public class Sorter
{
    public static Comparator<Group> GROUP_SORTER = new Comparator<Group>() {
        public int compare(Group o1, Group o2) {
            return o2.totalnew - o1.totalnew;
        }
    };
    
}

