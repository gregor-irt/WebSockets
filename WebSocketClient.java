/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
    private static class WebSocketClient {
        private OutputStream os;
        private InputStream is;
        private boolean isContinuation = false;
        final String encoding = "ISO-8859-1";
        private Socket socket ;
        private Writer writer ;
        private BufferedReader reader;

        public WebSocketClient(int port) {
            SocketAddress addr = new InetSocketAddress("localhost", port);
            socket = new Socket();
            try {
                socket.setSoTimeout(10000);
                socket.connect(addr, 10000);
                os = socket.getOutputStream();
                writer = new OutputStreamWriter(os, encoding);
                is = socket.getInputStream();
                Reader r = new InputStreamReader(is, encoding);
                reader = new BufferedReader(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void close() throws IOException {
            socket.close();
        }

        private void sendMessage(String message, boolean finalFragment)
                throws IOException {
            ByteChunk bc = new ByteChunk(8192);
            C2BConverter c2b = new C2BConverter(bc, "UTF-8");
            c2b.convert(message);
            c2b.flushBuffer();

            int len = bc.getLength();
            assertTrue(len < 126);

            byte first;
            if (isContinuation) {
                first = Constants.OPCODE_CONTINUATION;
            } else {
                first = Constants.OPCODE_TEXT;
            }
            if (finalFragment) {
                first = (byte) (0x80 | first);
            }
            os.write(first);

            os.write(0x80 | len);

            // Zero mask
            os.write(0);
            os.write(0);
            os.write(0);
            os.write(0);

            // Payload
            os.write(bc.getBytes(), bc.getStart(), len);

            os.flush();

            // Will the next frame be a continuation frame
            isContinuation = !finalFragment;
        }

        private String readMessage() throws IOException {
            ByteChunk bc = new ByteChunk(125);
            CharChunk cc = new CharChunk(125);

            // Skip first byte
            is.read();

            // Get payload length
            int len = is.read() & 0x7F;
            assertTrue(len < 126);

            // Read payload
            int read = 0;
            while (read < len) {
                read = read + is.read(bc.getBytes(), read, len - read);
            }

            bc.setEnd(len);

            B2CConverter b2c = new B2CConverter("UTF-8");
            b2c.convert(bc, cc, len);

            return cc.toString();
        }
    }
