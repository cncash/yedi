/*
 * Copyright (c) 2018 Wladislaw Mitzel
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package de.wlami.yeelight.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * UDPListener that works on the provided resources. It will listen on the socket and parse all
 * incoming UDP packets. The result is written into the provided list.<br>
 * <br>
 * This {@link Runnable} does not stop listening on the socket. You have to kill it from outside!
 */
@Slf4j
class UDPListener implements Runnable {

  /** default size of received UDP packets */
  public static final int DEFAULT_PACKET_SIZE = 4096;

  private DiscoveryResponseParser responseParser = new DiscoveryResponseParser();

  private DatagramSocket socket;
  private List<DiscoveredDevice> result;

  /**
   * Creates an UDPListener that works on the provided resources. It will listen on the socket and
   * parse all incoming UDP packets. The result is written into the provided list.<br>
   * <br>
   * This {@link Runnable} does not stop listening on the socket. You have to kill it from outside!
   * 
   * @param socket An UDP socket to listen on.
   * @param result A List that should get the results.
   */
  public UDPListener(DatagramSocket socket, List<DiscoveredDevice> result) {
    super();
    this.socket = socket;
    this.result = result;
  }

  @Override
  public void run() {
    try {
      while (true) {
        byte[] data = new byte[DEFAULT_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        socket.receive(packet);
        log.debug("received message from [{}].", packet.getSocketAddress());
        String resultString = new String(packet.getData(), StandardCharsets.UTF_8);
        log.trace(resultString);
        DiscoveredDevice device = responseParser.parseResponse(resultString);
        result.add(device);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
