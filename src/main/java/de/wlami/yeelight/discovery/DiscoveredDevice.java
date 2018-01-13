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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a Yeelight devices that have been discovered using the custom SSDP.
 */
@Data
@EqualsAndHashCode(of = "id")
public class DiscoveredDevice {

  public static final class Constants {
    public static final String MODEL_WHITE_BULB = "mono";
    public static final String MODEL_COLOR_BULB = "color";
    public static final String MODEL_LIGHTSTRIP = "stripe";
    public static final String MODEL_CEILING_LIGHT = "ceiling";

    public static final String POWER_ON = "on";
    public static final String POWER_OFF = "off";

    public static final int COLOR_MODE_RGB = 1;
    public static final int COLOR_MODE_COLOR_TEMPARATURE = 2;
    public static final int COLOR_MODE_HSV = 3;

  }

  /** this is the unique id of this device */
  private String id;
  /**
   * refresh interval. device will send next advertisement message after this perios
   */
  private Long cacheControl;
  /**
   * location of the device. something like "yeelight://192.168.192.201:55443"
   */
  private URI location;
  /**
   * type of device. See {@link Constants}.MODEL_* constants for possible values.
   */
  private String model;
  private Integer firmwareVersion;
  private List<String> supportedFunctions = new ArrayList<>();
  private String power;
  private Integer brightness;
  private Integer colorMode;
  private Integer colorTemparature;
  private Integer colorRGB;
  private Integer hue;
  private Integer saturation;
  private String name;
  private String server;
}
