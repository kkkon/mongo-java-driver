/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// BasicOutputBuffer.java

package org.bson.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

public class BasicOutputBuffer extends OutputBuffer {

    private int cur;
    private int size;
    private byte[] buffer = new byte[1024];

    @Override
    public void write(final byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        ensure(len);
        System.arraycopy(b, off, buffer, cur, len);
        cur += len;
        size = Math.max(cur, size);
    }

    @Override
    public void write(final int b) {
        ensure(1);
        buffer[cur++] = (byte) (0xFF & b);
        size = Math.max(cur, size);
    }

    @Override
    public void backpatchSize(final int messageSize) {
        writeInt(getPosition() - messageSize, messageSize);
    }

    @Override
    protected void backpatchSize(final int messageSize, final int additionalOffset) {
        writeInt(getPosition() - messageSize - additionalOffset, messageSize);
    }

    @Override
    public int getPosition() {
        return cur;
    }

    /**
     * @return size of data so far
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public void pipe(final OutputStream out) throws IOException {
        out.write(buffer, 0, size);
    }

    @Override
    public void pipe(final GatheringByteChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(buffer, 0, size));
    }

    @Override
    public void truncateToPosition(final int newPosition) {
        if (newPosition > size || newPosition < 0) {
            throw new IllegalArgumentException();
        }
        size = newPosition;
    }

    private void ensure(final int more) {
        final int need = cur + more;
        if (need < buffer.length) {
            return;
        }

        int newSize = buffer.length * 2;
        if (newSize <= need) {
            newSize = need + 128;
        }

        final byte[] n = new byte[newSize];
        System.arraycopy(buffer, 0, n, 0, size);
        buffer = n;
    }

    private void setPosition(final int position) {
        cur = position;
    }

    private void writeInt(final int pos, final int x) {
        final int save = getPosition();
        setPosition(pos);
        writeInt(x);
        setPosition(save);
    }
}