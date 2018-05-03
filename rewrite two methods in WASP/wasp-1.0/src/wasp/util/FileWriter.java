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

import java.io.File;
import java.io.IOException;

/**
 * This version of <code>FileWriter</code> creates all necessary but nonexistent parent directories
 * during the construction of <code>FileWriter</code> objects.
 * 
 * @author ywwong
 *
 */
public class FileWriter extends java.io.FileWriter {

	private FileWriter(File file) throws IOException {
		super(file);
	}
	
	private FileWriter(String filename) throws IOException {
		super(filename);
	}
	
	/**
	 * Constructs a <code>FileWriter</code> object given an abstract pathname.  All necessary but 
	 * nonexistent parent directories are also created.
	 * 
	 * @param file the abstract pathname.
	 * @return a newly-constructed <code>FileWriter</code> object based on the given abstract pathname.
	 * @throws IOException if the file exists but is a directory rather than a regular file, or cannot 
	 * be opened for any other reason.
	 */
	public static FileWriter createNew(File file) throws IOException {
		if (file.getParentFile() != null)
			file.getParentFile().mkdirs();
		return new FileWriter(file);
	}
	
	/**
	 * Constructs a <code>FileWriter</code> object given a file name.  All necessary but nonexistent
	 * parent directories are also created.
	 * 
	 * @param filename the system-dependent file name.
	 * @return a newly-constructed <code>FileWriter</code> object based on the given file name.
	 * @throws IOException if the named file exists but is a directory rather than a regular file, or 
	 * cannot be opened for any other reason.
	 */
	public static FileWriter createNew(String filename) throws IOException {
		File file = new File(filename);
		if (file.getParentFile() != null)
			file.getParentFile().mkdirs();
		return new FileWriter(filename);
	}
	
}
