/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.xkit.app.helper;

/**
 * The Variable Length Array implementation that just supports INT elements.
 * 
 * @author rocwon@gmail.com
 */
public final class VLArray {
	private int[] data;
	public int length;
	private int maxIndex;
	
	public VLArray() {
		this(64);
	}
	
	public VLArray(int len) {
		data = new int[len <= 0 ? 32 : len];
		this.length = data.length;
	}
	
	/**
	 * Wrap a fix length array to a variable length array
	 * */
	public VLArray(int[] array) {
		data = array != null ? array : new int[64];
		this.length = this.data.length;
		this.maxIndex = this.length - 1;
	}
	
	public int get(int index) {
		return this.data[index];
	}
	
	public void append(int item) {
		this.put(maxIndex + 1, item);
	}
	
	public void put(int index, int item) {
		if(index >= this.length) {
			extendsCapacity(index);
		}
		this.data[index] = item;
		
		if(index > maxIndex) {
			maxIndex = index;
		}
	}
	
	/**
	 * @return Returns the original INT array with actual length.
	 * */
	public int[] get() {
		var len = maxIndex + 1;
		if(len == data.length) return data;
		var result = new int[len];
		System.arraycopy(data, 0, result, 0, len);
		return result;
	}
	
	/**
	 * Slice the array from the specific indexes start(inclusive) to end(inclusive).
	 * @see Slicer.slice
	 * */
	public int[] get(int bgn, int end) {
		return Slicer.slice(this.data, bgn, end);
	}
	
	/**
	 * Slice the array from the specific indexes start(inclusive) to end(exclusive).
	 * @see Slicer.slice
	 * */
	public int[] slice(int bgn, int end) {
		return get(bgn, end - 1);
	}
	
	private void extendsCapacity(int min) {
		var tmp = this.data;
		int ext = min + (min <= 512 ? (min >> 1) : (min >> 2));
		length += length <= 512 ? (length >> 1) : (length >> 2);
		
		if(length <= ext) length = ext;
		
		this.data = new int[this.length]; //Resize
		System.arraycopy(tmp, 0, data, 0, tmp.length);
	}
}