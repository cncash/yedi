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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UDPListenerTest {

  @Test
  public void run_mockedSocket_returnsReceivedDevices() throws Exception {
    DatagramSocket socket = mock(DatagramSocket.class);

    // mock the udp socket
    doAnswer(new Answer<DatagramPacket>() {
      int i = 0;
      // that are our mocked udp packets
      byte[][] responses = {
          ("HTTP/1.1 200 OK\n" //
              + "id: 0x123\n" //
              + "support: get_prop set_default\n" //
              + "power: on\n" //
              + "bright: 100\n" //
              + "name: lalala\n").getBytes(StandardCharsets.UTF_8),
          ("HTTP/1.1 200 OK\n" //
              + "id: 0x456\n" //
              + "support: get_prop\n" //
              + "power: off\n" //
              + "bright: 56\n" //
              + "name: asdasd\n").getBytes(StandardCharsets.UTF_8),
          ("HTTP/1.1 200 OK\n" //
              + "id: 0x789\n" //
              + "support: get_prop set_default\n" //
              + "power: on\n" //
              + "bright: 12\n" //
              + "name: xcvxcv\n").getBytes(StandardCharsets.UTF_8)};

      @Override
      public DatagramPacket answer(InvocationOnMock invocation) throws Throwable {
        if (i < responses.length) {

          // when called
          DatagramPacket packet = invocation.getArgument(0);
          // let's just iterate over the predefined responses
          packet.setData(responses[i++]);
          packet.setSocketAddress(new InetSocketAddress(12345));
          return null;
        } else {
          while (true) {
            Thread.sleep(100);
          }
        }
      }
    }).when(socket).receive(ArgumentMatchers.any());

    List<DiscoveredDevice> result = new ArrayList<>();
    UDPListener udpListener = new UDPListener(socket, result);
    Future<?> future = Executors.newSingleThreadExecutor().submit(udpListener);
    try {
      future.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException e) {
      // this shouldn't happen!
      throw e;
    } catch (TimeoutException e) {
      // we expect this as our runnable does not stop on its own
    }

    DiscoveredDevice first = new DiscoveredDevice();
    first.setId("0x123");
    DiscoveredDevice second = new DiscoveredDevice();
    second.setId("0x456");
    DiscoveredDevice third = new DiscoveredDevice();
    third.setId("0x789");

    assertThat(result, Matchers.containsInAnyOrder(first, second, third));

  }

}
