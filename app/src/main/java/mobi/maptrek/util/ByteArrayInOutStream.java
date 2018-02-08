/*
 * Copyright 2018 Andrey Novikov
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mobi.maptrek.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * This class extends the ByteArrayOutputStream by
 * providing a method that returns a new ByteArrayInputStream
 * which uses the internal byte array buffer. This buffer
 * is not copied, so no additional memory is used. After
 * creating the ByteArrayInputStream the instance of the
 * ByteArrayInOutStream can not be used anymore.
 * <p>
 * The ByteArrayInputStream can be retrieved using <code>getInputStream()</code>.
 * @author Nick Russler
 */
public class ByteArrayInOutStream extends ByteArrayOutputStream {
    /**
     * Creates a new ByteArrayInOutStream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
    public ByteArrayInOutStream() {
        super();
    }

    /**
     * Creates a new ByteArrayInOutStream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param   size   the initial size.
     * @exception  IllegalArgumentException if size is negative.
     */
    public ByteArrayInOutStream(int size) {
        super(size);
    }

    /**
     * Creates a new ByteArrayInputStream that uses the internal byte array buffer
     * of this ByteArrayInOutStream instance as its buffer array. The initial value
     * of pos is set to zero and the initial value of count is the number of bytes
     * that can be read from the byte array. The buffer array is not copied. This
     * instance of ByteArrayInOutStream can not be used anymore after calling this
     * method.
     * @return the ByteArrayInputStream instance
     */
    public ByteArrayInputStream getInputStream() {
        // create new ByteArrayInputStream that respects the current count
        ByteArrayInputStream in = new ByteArrayInputStream(this.buf, 0, this.count);

        // set the buffer of the ByteArrayOutputStream
        // to null so it can't be altered anymore
        this.buf = null;

        return in;
    }
}
