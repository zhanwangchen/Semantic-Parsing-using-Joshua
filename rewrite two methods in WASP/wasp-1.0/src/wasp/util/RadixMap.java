/*
 * Copyright 2006 Yuk Wah Wong (The University of Texas at Austin).
 * 
 * This file is part of the WASP distribution.
 *
 * WASP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * WASP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WASP; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package wasp.util;

/**
 * An implementation of mappings from non-negative integers to objects.
 *  
 * @author ywwong
 *
 */
public class RadixMap {

    private static class Node {
        public Object obj;
        public Node zero;
        public Node one;
        public Node() {
            obj = null;
            zero = null;
            one = null;
        }
    }
    
    private Node root;
    private int nobjs;
    
    public RadixMap() {
        root = new Node();
        nobjs = 0;
    }
    
    private Node getNode(int key, boolean add) {
        Node node = root;
        do {
            Node child;
            boolean bit = (key&1) == 1;
            if (bit)
                child = node.one;
            else
                child = node.zero;
            if (child == null && add) {
                child = new Node();
                if (bit)
                    node.one = child;
                else
                    node.zero = child;
            }
            key >>= 1;  // assuming key >= 0
            node = child;
        } while (key != 0 && node != null);
        return node;
    }
    
    public Object get(int key) {
        Node node = getNode(key, false);
        if (node == null)
            return null;
        else
            return node.obj;
    }
    
    public boolean containsKey(int key) {
        Node node = getNode(key, false);
        return node != null && node.obj != null;
    }

    /**
     * 
     * @param key a non-negative integer.
     * @param value the object associated with <code>key</code>, which must not be <code>null</code>.
     */
    public void put(int key, Object value) {
        Node node = getNode(key, true);
        if (node.obj == null)
            ++nobjs;
        node.obj = value;
    }
    
    public void remove(int key) {
        Node node = getNode(key, false);
        if (node != null && node.obj != null) {
            node.obj = null;
            --nobjs;
        }
    }
    
    public boolean isEmpty() {
        return nobjs == 0;
    }
    
    public int size() {
        return nobjs;
    }
    
    public void clear() {
        root = new Node();
        nobjs = 0;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        toString(root, 0, 0, sb, new Bool(true));
        sb.append('}');
        return sb.toString();
    }
    
    private void toString(Node node, int key, int digit, StringBuffer sb, Bool first) {
        if (node.obj != null) {
            if (first.val)
                first.val = false;
            else
                sb.append(", ");
            sb.append(key);
            sb.append('=');
            sb.append(node.obj);
        }
        if (node.zero != null)
            toString(node.zero, key, digit+1, sb, first);
        if (node.one != null)
            toString(node.one, key|(1<<digit), digit+1, sb, first);
    }

}
