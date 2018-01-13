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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.net.URI;
import java.net.URISyntaxException;
import org.hamcrest.Matchers;
import org.junit.Test;
import lombok.val;

public class DiscoveryResponseParserTest {

  public static final String RESPONSE1 = //
      "HTTP/1.1 200 OK\n" //
          + "Cache-Control: max-age=3600\n" //
          + "Date: \n" //
          + "Ext: \n" //
          + "Location: yeelight://192.168.2.11:55443\n" //
          + "Server: POSIX UPnP/1.0 YGLC/1\n" //
          + "id: 0x00000abcd1231233\n" //
          + "model: stripe\n" //
          + "fw_ver: 40\n" //
          + "support: get_prop set_default set_power toggle set_bright start_cf stop_cf set_scene cron_add cron_get cron_del set_ct_abx set_rgb set_hsv set_adjust set_music set_name\n" //
          + "power: on\n" //
          + "bright: 100\n" //
          + "color_mode: 1\n" //
          + "ct: 4000\n" //
          + "rgb: 13158400\n" //
          + "hue: 2\n" //
          + "sat: 100\n" //
          + "name: lalala\n";

  @Test
  public void parseResponse_fullInput_returnsAllFields() throws URISyntaxException {
    DiscoveryResponseParser parser = new DiscoveryResponseParser();
    DiscoveredDevice response = parser.parseResponse(RESPONSE1);
    assertEquals(new Long(3600), response.getCacheControl());
    assertEquals(new URI("yeelight://192.168.2.11:55443"), response.getLocation());
    assertEquals("POSIX UPnP/1.0 YGLC/1", response.getServer());
    assertEquals("0x00000abcd1231233", response.getId());
    assertEquals(DiscoveredDevice.Constants.MODEL_LIGHTSTRIP, response.getModel());
    assertEquals(new Integer(40), response.getFirmwareVersion());
    assertThat(response.getSupportedFunctions(),
        Matchers.containsInAnyOrder("get_prop", "set_default", "set_power", "toggle", "set_bright",
            "start_cf", "stop_cf", "set_scene", "cron_add", "cron_get", "cron_del", "set_ct_abx",
            "set_rgb", "set_hsv", "set_adjust", "set_music", "set_name"));
    assertEquals(DiscoveredDevice.Constants.POWER_ON, response.getPower());
    assertEquals(new Integer(100), response.getBrightness());
    assertEquals(new Integer(DiscoveredDevice.Constants.COLOR_MODE_RGB), response.getColorMode());
    assertEquals(new Integer(4000), response.getColorTemparature());
    assertEquals(new Integer(13158400), response.getColorRGB());
    assertEquals(new Integer(2), response.getHue());
    assertEquals(new Integer(100), response.getSaturation());
    assertEquals("lalala", response.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseResponse_nullParam_throwsException() {
    new DiscoveryResponseParser().parseResponse(null);
  }

  @Test
  public void parseCacheControl_validLine_returnsCorrectLong() {
    val INPUT = "Cache-Control: max-age=13600";
    val result = new DiscoveryResponseParser().parseCacheControl(INPUT);
    assertEquals(new Long(13600L), result);
  }

  @Test
  public void parseCacheControl_mixedCase_returnsCorrectLong() {
    val INPUT = "cAcHe-coNTRol: max-age=123";
    val RESULT = new DiscoveryResponseParser().parseCacheControl(INPUT);
    assertEquals(new Long(123), RESULT);
  }

  @Test
  public void parseCacheControl_unknownData_returnsNull() {
    val INPUT = "Cache-Control: sdf";
    val RESULT = new DiscoveryResponseParser().parseCacheControl(INPUT);
    assertNull(RESULT);
  }

  @Test
  public void parseLocation_validIPAddress_returnsUri() throws URISyntaxException {
    val INPUT = "Location: yeelight://192.168.2.11:55443";
    val RESULT = new DiscoveryResponseParser().parseLocation(INPUT);
    assertEquals(new URI("yeelight://192.168.2.11:55443"), RESULT);
  }

  @Test
  public void parseLocation_invalidIPAddress_returnsUri() throws URISyntaxException {
    val INPUT = "Location: yeelight://256.300.2.11:55443";
    val RESULT = new DiscoveryResponseParser().parseLocation(INPUT);
    assertEquals(new URI("yeelight://256.300.2.11:55443"), RESULT);
  }

  @Test
  public void getValuePart_validLine_returnsValue() {
    val INPUT = "header: my Value";
    val RESULT = new DiscoveryResponseParser().getValuePart(INPUT);
    assertEquals("my Value", RESULT);
  }

  @Test
  public void getValuePart_emptyValue_returnsEmptyString() {
    val INPUT = "header:";
    val RESULT = new DiscoveryResponseParser().getValuePart(INPUT);
    assertEquals("", RESULT);
  }

  @Test
  public void getValuePart_noHeader_returnsNull() {
    val INPUT = ": sadflkj";
    val RESULT = new DiscoveryResponseParser().getValuePart(INPUT);
    assertNull(RESULT);
  }

  @Test
  public void parseInteger_FW1_returns1() {
    val INPUT = "fw: 1";
    val RESULT = new DiscoveryResponseParser().parseInteger(INPUT);
    assertEquals(new Integer(1), RESULT);
  }

  @Test
  public void parseInteger_stringValue_returnsNull() {
    val INPUT = "fw: super-firmware";
    val RESULT = new DiscoveryResponseParser().parseInteger(INPUT);
    assertNull(RESULT);
  }

  @Test
  public void parseSupportedFunctions_validThreeFunctions_listWithThreeElements() {
    val INPUT = "support: get_prop set_default set_power";
    val RESULT = new DiscoveryResponseParser().parseSupportedFunctions(INPUT);
    assertThat(RESULT, Matchers.containsInAnyOrder("get_prop", "set_default", "set_power"));
  }

  @Test
  public void parseSupportedFunctions_noValues_emptyList() {
    val INPUT = "support:   ";
    val RESULT = new DiscoveryResponseParser().parseSupportedFunctions(INPUT);
    assertThat(RESULT, Matchers.empty());
  }

}
