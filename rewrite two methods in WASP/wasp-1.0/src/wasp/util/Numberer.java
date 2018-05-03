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

import java.util.HashMap;

/**
 * A data structure that maps objects to non-negative integer IDs, and vice versa.
 * 
 * @author ywwong
 *
 */
public class Numberer {

    private static final int INC = 128;
    private static final int BIG_INC = 1024;
    
    private HashMap objToId;
    private Object[] idToObj;
    private int nextId;

    /**
     * @param firstId the first ID to use.  It must not exceed <code>127</code>.
     */
    public Numberer(int firstId) {
        objToId = new HashMap();
        idToObj = new Object[INC];
        nextId = firstId;
    }

    public Numberer() {
    	this(0);
    }
    
    /**
     * Returns the ID of the specified object.  If the specified object is not found and the
     * <code>add</code> argument is true, then the object is mapped to a new ID.  Otherwise, 
     * <code>-1</code> is returned.
     *  
     * @param o the object to look for.
     * @param add indicates if a new ID is created when the <code>o</code> argument is not found.
     * @return the ID of the specified object; <code>-1</code> if there is no such ID.
     */
    public int getId(Object o, boolean add) {
        Int id = (Int) objToId.get(o);
        if (id == null && add) {
            id = new Int(nextId++);
            objToId.put(o, id);
            if (id.val == idToObj.length) {
                int inc = (id.val>=BIG_INC) ? BIG_INC : INC;
                idToObj = Arrays.resize(idToObj, id.val+inc);
            }
            idToObj[id.val] = o;
        }
        return (id==null) ? -1 : id.val;
    }

    /**
     * Returns the object with the specified ID.  If there is no such ID, then <code>null</code> is
     * returned.
     * 
     * @param id an object ID.
     * @return the object with the specified ID; <code>null</code> when no such ID exists.
     */
    public Object getObj(int id) {
        return (0<=id && id<nextId) ? idToObj[id] : null;
    }
    
    /**
     * Returns the next ID to assign to new objects.
     * 
     * @return the next ID to assign to new objects.
     */
    public int getNextId() {
        return nextId;
    }
    
    /**
     * Creates a new mapping for the specified object.  If a mapping already exists, then no new ID is
     * assigned and <code>false</code> is returned.
     * 
     * @param o an object to add. 
     * @return <code>true</code> if a new ID is assigned to the specified object; <code>false</code>
     * otherwise.
     */
    public boolean addObj(Object o) {
    	int last = nextId;
    	return getId(o, true) == last;
    }
    
}
