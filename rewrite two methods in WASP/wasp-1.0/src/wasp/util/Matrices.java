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
 * Common matrix operations.
 * 
 * @author ywwong
 *
 */
public class Matrices {

	/**
	 * Indicates if the specified boolean matrices are equal.
	 * 
	 * @param mat1 a boolean matrix.
	 * @param mat2 a boolean matrix.
	 * @return <code>true</code> if the two matrices are equal; <code>false</code> otherwise.
	 */
	public static boolean equal(boolean[][] mat1, boolean[][] mat2) {
		if (mat1.length != mat2.length)
			return false;
		for (int i = 0; i < mat1.length; ++i) {
			if (mat1[i].length != mat2[i].length)
				return false;
			for (int j = 0; j < mat1[i].length; ++j)
				if (mat1[i][j] != mat2[i][j])
					return false;
		}
		return true;
	}
	
	/**
	 * Returns the hash code for the specified boolean matrix.
	 * 
	 * @param mat a boolean matrix.
	 * @return the hash code for the <code>mat</code> argument.
	 */
	public static int hashCode(boolean[][] mat) {
		int hash = 1;
		for (int i = 0; i < mat.length; ++i)
			for (int j = 0; j < mat[i].length; ++j)
				hash = 31*hash + ((mat[i][j]) ? 1 : 0);
		return hash;
	}
	
	/**
	 * Returns the reflexive-transitive closure of the specified binary relation.  Given a directed graph, 
	 * represented by an adjacency matrix, <code>rel</code>, this method computes the matrix 
	 * <code>T</code>, where <code>T[i][j]</code> is <code>true</code> if there is a path (possibly of 
	 * zero length) from <code>i</code> to <code>j</code>.
	 * 
	 * @param rel a binary relation.
	 * @return the transitive closure of the <code>rel</code> argument.
	 */
	public static boolean[][] reflexiveTransitive(boolean[][] rel) {
        int n = rel.length;
        boolean[][][] t = new boolean[2][n][n];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j)
            	t[0][i][j] = i==j || rel[i][j];
        for (int k = 1; k <= n; ++k) {
            int x = (k-1)%2;
            int y = k%2;
            for (int i = 0; i < n; ++i)
                for (int j = 0; j < n; ++j)
                    t[y][i][j] = t[x][i][j] || (t[x][i][k-1] && t[x][k-1][j]);
        }
        return t[n%2];
	}
	
	/**
	 * Returns the transitive closure of the specified binary relation.  Given a directed graph, 
	 * represented by an adjacency matrix, <code>rel</code>, this method computes the matrix 
	 * <code>T</code>, where <code>T[i][j]</code> is <code>true</code> if there is a path of length 
	 * greater than or equal to 1 from <code>i</code> to <code>j</code>.
	 * 
	 * @param rel a binary relation.
	 * @return the transitive closure of the <code>rel</code> argument.
	 */
	public static boolean[][] transitive(boolean[][] rel) {
        int n = rel.length;
        boolean[][][] t = new boolean[2][n][n];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j)
            	t[0][i][j] = rel[i][j];
        for (int k = 1; k <= n; ++k) {
            int x = (k-1)%2;
            int y = k%2;
            for (int i = 0; i < n; ++i)
                for (int j = 0; j < n; ++j)
                    t[y][i][j] = t[x][i][j] || (t[x][i][k-1] && t[x][k-1][j]);
        }
        return t[n%2];
	}
	
}
