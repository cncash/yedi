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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements the Yeelight-specific SSDP lookup of devices on the local network. It uses UDP
 * multicast at address <code>239.255.255.250</code> which means that it will only work in the
 * current local network, as broadcast packets are not forwared to other networks.<br>
 * <br>
 * Call {@link #discover(long, TimeUnit, NetworkInterface)} in order to search for Yeelight devices.
 */
@Slf4j
public class DiscoveryClient {

  /** broadcast address for SSDP messages */
  public static final String DISCOVERY_ADDRESS = "239.255.255.250";
  /** custom yeelight port for SSDP messages (less traffic than on 1900) */
  public static final int DISCOVERY_PORT = 1982;
  /** constant discovery message */
  public static final byte[] DISCOVERY_MESSAGE = ("M-SEARCH * HTTP/1.1\r\n" //
      + "HOST: 239.255.255.250:1982\r\n" //
      + "MAN: \"ssdp:discover\"\r\n" //
      + "ST: wifi_bulb\r\n").getBytes(StandardCharsets.UTF_8);

  /**
   * Searches for a predefined time for Yeelight devices in the LAN. Usually they respond within
   * fractions of a second. As multicast messages need to know on which interface they should be
   * sent you have to provide a network interface. <br>
   * <b>They have to have activated LAN mode! Otherwise they cannot be discovered/controlled
   * locally. You can activate it in the Yeelight app.</b> <br>
   * <br>
   * You can lookup a network interface by its IP address. Example:<br>
   * <code>NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.0.53"));</code>
   * 
   * 
   * @param timeout Timeout for responses. As the devices usually respond within milliseconds some
   *        seconds should be enough.
   * @param unit The time unit you want to use.
   * @param networkInterface The network interface you want to use for the discovery message.
   * @throws IOException
   */
  public List<DiscoveredDevice> discover(long timeout, TimeUnit unit,
      NetworkInterface networkInterface) {
    log.debug("trying to discover yeelights");
    log.debug("using network interface [{}]", networkInterface);
    final List<DiscoveredDevice> result = new ArrayList<>();
    Future<?> future = null;
    try (MulticastSocket socket = new MulticastSocket()) {
      log.debug("Created new MulticastSocket [{}]", socket);
      socket.setNetworkInterface(networkInterface);
      sendDiscoveryPacket(socket);
      log.info("sent discovery message on interface [{}]", networkInterface);
      ExecutorService executor = Executors.newSingleThreadExecutor();
      future = executor.submit(new UDPListener(socket, result));
      future.get(timeout, unit);
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      // this is the desired processing path as we wait for the future
      // until we time out!
      log.debug("we have waited [{}][{}]. Discovered devices: [{}]", timeout, unit, result);
      if (future != null) {
        future.cancel(false);
      }
    }
    return result;
  }

  protected void sendDiscoveryPacket(DatagramSocket socket)
      throws UnknownHostException, IOException {

    InetAddress broadcastGroup = InetAddress.getByName(DISCOVERY_ADDRESS);
    byte[] searchPayload = createSearchRequest();
    DatagramPacket searchPacket =
        new DatagramPacket(searchPayload, searchPayload.length, broadcastGroup, DISCOVERY_PORT);
    socket.send(searchPacket);
  }

  /**
   * Returns the UDP payload that is required for doing a search request.
   * 
   * @return String representation of the payload.
   */
  protected byte[] createSearchRequest() {
    return DISCOVERY_MESSAGE;
  }

}
