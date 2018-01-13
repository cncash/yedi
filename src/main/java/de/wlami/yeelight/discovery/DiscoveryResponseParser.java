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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Gets the contents of a discovery response as a string and extracts the information into a
 * {@link DiscoveredDevice}.
 */
@Slf4j
class DiscoveryResponseParser {

  public DiscoveredDevice parseResponse(String data) {
    if (data == null) {
      throw new IllegalArgumentException("data must not be null!");
    }
    DiscoveredDevice result = new DiscoveredDevice();
    try (Scanner scanner = new Scanner(data)) {
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        String[] splittedByColon = line.split(":");
        if (splittedByColon.length > 1) {
          String firstElement = splittedByColon[0].toLowerCase();
          switch (firstElement) {
            case "cache-control":
              long cacheControl = parseCacheControl(line);
              result.setCacheControl(cacheControl);
              break;
            case "location":
              URI location = parseLocation(line);
              result.setLocation(location);
              break;
            case "server":
              String server = getValuePart(line);
              result.setServer(server);
              break;
            case "id":
              String id = getValuePart(line);
              result.setId(id);
              break;
            case "model":
              String model = getValuePart(line);
              result.setModel(model);
              break;
            case "fw_ver":
              int firmwareVersion = parseInteger(line);
              result.setFirmwareVersion(firmwareVersion);
              break;
            case "support":
              List<String> supportedFunctions = parseSupportedFunctions(line);
              result.setSupportedFunctions(supportedFunctions);
              break;
            case "power":
              String power = getValuePart(line);
              result.setPower(power);
              break;
            case "bright":
              Integer brightness = parseInteger(line);
              result.setBrightness(brightness);
              break;
            case "color_mode":
              Integer colorMode = parseInteger(line);
              result.setColorMode(colorMode);
              break;
            case "ct":
              Integer colorTemparature = parseInteger(line);
              result.setColorTemparature(colorTemparature);
              break;
            case "rgb":
              Integer rgb = parseInteger(line);
              result.setColorRGB(rgb);
              break;
            case "hue":
              Integer hue = parseInteger(line);
              result.setHue(hue);
              break;
            case "sat":
              Integer saturation = parseInteger(line);
              result.setSaturation(saturation);
              break;
            case "name":
              String name = getValuePart(line);
              result.setName(name);
              break;
            default:
              break;
          }
        }
      }
    }
    return result;
  }

  protected List<String> parseSupportedFunctions(String line) {
    String valuePart = getValuePart(line);
    List<String> result = new ArrayList<>();
    if (!valuePart.isEmpty()) {
      result.addAll(Arrays.asList(valuePart.split(" ")));
    }
    return result;
  }

  protected Integer parseInteger(String line) {
    Integer result = null;
    try {
      String valuePart = getValuePart(line);
      result = Integer.parseInt(valuePart);
    } catch (NumberFormatException e) {
      log.debug("could not parse integer!", e);
    }
    return result;
  }

  protected URI parseLocation(String line) {
    URI result = null;
    String value = getValuePart(line);
    try {
      result = new URI(value);
    } catch (URISyntaxException e) {
      log.debug("could not parse location url", e);
    }
    return result;
  }

  protected String getValuePart(String line) {
    int positionOfFirstColon = line.indexOf(':');
    String result = null;
    if (positionOfFirstColon > 1) {
      result = line.length() >= positionOfFirstColon + 1
          ? line.substring(positionOfFirstColon + 1).trim() : "";
    }
    return result;
  }

  protected static final Pattern CACHE_CONTROL_PATTERN =
      Pattern.compile("Cache-Control\\: max-age=(\\d+)\\s?", Pattern.CASE_INSENSITIVE);

  protected Long parseCacheControl(String line) {
    Long result = null;
    Matcher matcher = CACHE_CONTROL_PATTERN.matcher(line);
    if (matcher.find()) {
      result = Long.parseLong(matcher.group(1));
    }
    return result;
  }

}
