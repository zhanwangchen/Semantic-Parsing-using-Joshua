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
 * An implementation of vectors of bits.  In contrast to <code>java.util.BitSet</code>, the vectors never
 * grow in size.
 * 
 * @author ywwong
 *
 */
public class BitSet implements Copyable {

    private byte[] data;
    private short length;
    
    public BitSet(short length) {
        data = null;
        this.length = length;
    }
    
    private BitSet() {}
    
    public boolean equals(Object o) {
        if (o instanceof BitSet) {
            BitSet set = (BitSet) o;
            if (length != set.length)
            	return false;
            if (data == null)
            	return set.data == null;
            else
            	return set.data != null && Arrays.equal(data, set.data);
        }
        return false;
    }
    
    public int hashCode() {
        if (data == null)
            return 0;
        else {
        	long h = 1234;
            for (short i = (short) (data.length-1); i >= 0; --i)
                 h ^= data[i] * (i+1);
            return (int) ((h>>32) ^ h);
        }
    }
    
    public Object copy() {
        BitSet copy = new BitSet();
        copy.data = (data==null) ? null : (byte[]) data.clone();
        copy.length = length;
        return copy;
    }
    
    public boolean get(short index) {
        if (data == null)
            return false;
        else
            return (data[index>>3] & ((byte)1<<(index&7))) != (byte)0;   
    }
    
    public void set(short index, boolean value) {
        if (value) {
            if (data == null)
                data = new byte[((length-1)>>3)+1];
            data[index>>3] |= ((byte)1<<(index&7));
        } else {
            if (data == null)
                return;
            data[index>>3] &= ~((byte)1<<(index&7));
            if (cardinality() == 0)
                data = null;
        }
    }
    
    public void setAll(boolean value) {
    	if (value) {
            if (data == null)
                data = new byte[((length-1)>>3)+1];
    		for (short i = 0; i < (length>>3); ++i)
    			data[i] = ~((byte)0);
    		for (short i = 0; i < (length&7); ++i)
    			data[data.length-1] |= ((byte)1<<i);
    	} else
    		data = null;
    }
    
    public boolean isSubsetOf(BitSet set) {
        if (length != set.length)
            return false;
        if (data == null)
            return true;
        if (set.data == null)
            return false; 
        for (short i = 0; i < data.length; ++i)
            if ((data[i] & ~set.data[i]) != (byte)0)
                return false;
        return true;
    }
    
    /**
     * Returns the intersection of this bit vector and the specified bit vector.  This operation is
     * non-destructive.
     *  
     * @param set a bit vector.
     * @return the intersection of this bit vector and the <code>set</code> argument.
     */
    public BitSet intersect(BitSet set) {
        if (length != set.length)
            return null;
        if (data == null || set.data == null)
            return new BitSet(length);
        byte[] d = new byte[data.length];
        boolean isEmpty = true;
        for (short i = 0; i < data.length; ++i) {
            d[i] = (byte) (data[i] & set.data[i]);
            if (d[i] != (byte)0)
                isEmpty = false;
        }
        BitSet x = new BitSet();
        x.data = (isEmpty) ? null : d;
        x.length = length;
        return x;
    }
    
    /**
     * Constructs the union of this bit vector and the specified bit vector.  This operation is
     * destructive.
     * 
     * @param set a bit vector.
     */
    public void union(BitSet set) {
        if (length != set.length)
            return;
        if (set.data == null)
            return;
        if (data == null) {
            data = (byte[]) set.data.clone();
            return;
        }
        for (short i = 0; i < data.length; ++i)
            data[i] |= set.data[i];
    }
    
    public short length() {
    	return length;
    }
    
    public short cardinality() {
        if (data == null)
            return 0;
        short c = 0;
        for (short i = 0; i < data.length; ++i) {
            byte b = data[i];
            while (b != 0) {
                if ((b&((byte)1)) != 0)
                    ++c;
                b = (byte)((b>>1)&127);
            }
        }
        return c;
    }
    
    public boolean isEmpty() {
        return data == null;
    }
    
    public boolean isFull() {
    	if (length == 0)
    		return true;
    	else if (data == null)
    		return false;
    	else {
    		for (short i = 0; i < (length>>3); ++i)
    			if (data[i] != ~((byte)0))
    				return false;
    		for (short i = 0; i < (length&7); ++i)
    			if ((data[data.length-1] & ((byte)1<<i)) == (byte)0)
    				return false;
    		return true;
    	}
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        boolean first = true;
        for (short i = 0; i < length; ++i)
            if (get(i)) {
                if (first)
                    first = false;
                else
                    sb.append(", ");
                sb.append(i);
            }
        sb.append('}');
        return sb.toString();
    }
    
}
