# Yedi - Yeelight Discovery library

This project let's you search for Yeelight devices on the local network. 

See [Yeelight_Inter-Operation_Spec.pdf](http://www.yeelight.com/download/Yeelight_Inter-Operation_Spec.pdf) for technical documentation on Yeelight device discovery.

**Yeelight is a Trademark of Qingdao Yeelink Information Technology Co., Ltd. - this project is not associated with it.**

## Getting Started

Yedi is a maven module that you can add to your project as dependency.

For maven:

	<dependency>
		<groupId>de.wlami.yedi</groupId>
		<artifactId>yeelight-discovery</artifactId>
		<version>1.0.0-SNAPSHOT</version>
	</dependency>

**CAUTION**: Yedi isn't available in maven-central, yet. You have to build it locally!

**CAUTION**: Before you can discover any Yeelight device you have to enable LAN-mode for each device.
You can do so in the Yeelight app.

### Using Yedi:

Using Yedi is pretty straight forward:

	DiscoveryClient client = new DiscoveryClient();
	NetworkInterface networkInterface =
	    NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.xxx.xxx"));
	List<DiscoveredDevice> discover = client.discover(1, TimeUnit.SECONDS, networkInterface);

Create a new `DiscoveryClient`. As we are using a discovery protocol based on SSDP we emit UDP multicast
packets. For them to be reliably sent to the correct network you have to provide the correct 
`NetworkInterface`. You can e.g. look it up using its IP address `NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.xxx.xxx"))`.
Now you can call the actual `client.discover(1, TimeUnit.SECONDS, networkInterface);` method. You have to
define a timeout to wait for the responses. As the devices usually respons within some milliseconds a few
seconds should suffice. Each device has an unique ID which identifies it. 

### Prerequisites

#### Java 8

Yedi uses Java 8.

#### lombok

Yedi uses lombok in order to reduce the amount of boilerplate code. It might be necessary
to install it into your IDE. Please see https://projectlombok.org/ for instructions on how
to do so. It runs as it is in Maven when invoked from commandline.

#### maven

You need maven in order to build Yedi.

### Installing

1. Install maven in order to be able to start the build.
2. run `mvn clean install` in order to install YEDI into your local maven repository.

If you you want to develop with IDEA or Eclipse:

* Install [Lombok](https://projectlombok.org/), otherwise you will get a lot of
compiler errors.


### Running the tests

The unit tests can be run with `mvn test`. There are no further dependencies. 

There is a integration test called `de.wlami.yeelight.discovery.DiscoveryClientIntegrationTest.discover_integrationTest()`
which will scan the local network for Yeelight devices. Before you can start it you have to adapt the test to 
use the IP address of your network interface that is connected to the same network as the Yeelight devices. This
is required as we are using UDP multicast for discovery.

	[...]
	public class DiscoveryClientIntegrationTest {
	
	  private String ipAddressOfInterface = "192.168.192.53";
	[...]

Now you can start the test with:

	mvn -Pintegration test
	
This will output a result similar to:

	00:36:32.888 [pool-1-thread-1] DEBUG d.w.yeelight.discovery.UDPListener - received message from [/192.168.xxx.xxx:49159].
	00:36:32.895 [pool-1-thread-1] DEBUG d.w.yeelight.discovery.UDPListener - received message from [/192.168.xxx.xxx:49158].
	00:36:33.888 [main] DEBUG d.w.y.discovery.DiscoveryClient - we have waited [1][SECONDS]. Discovered devices: [[DiscoveredDevice
	(id=0x0000000xxxxxxxxx, cacheControl=3600, location=yeelight://192.168.xxx.xxx:55443, model=stripe, firmwareVersion=40, support
	edFunctions=[get_prop, set_default, set_power, toggle, set_bright, start_cf, stop_cf, set_scene, cron_add, cron_get, cron_del,
	set_ct_abx, set_rgb, set_hsv, set_adjust, set_music, set_name], power=off, brightness=100, colorMode=1, colorTemparature=4000,
	colorRGB=13158400, hue=0, saturation=100, name=, server=POSIX UPnP/1.0 YGLC/1), DiscoveredDevice(id=0x0000000xxxxxxxxx, cacheCo
	ntrol=3584, location=yeelight://192.168.xxx.xxx:55443, model=color, firmwareVersion=57, supportedFunctions=[get_prop, set_defaul
	t, set_power, toggle, set_bright, start_cf, stop_cf, set_scene, cron_add, cron_get, cron_del, set_ct_abx, set_rgb, set_hsv, set
	_adjust, adjust_bright, adjust_ct, adjust_color, set_music, set], power=off, brightness=100, colorMode=2, colorTemparature=4000
	, colorRGB=16711680, hue=359, saturation=100, name=, server=POSIX UPnP/1.0 YGLC/1)]]
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.299 sec
	
	Results :
	
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
	
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 2.838 s
	[INFO] Finished at: 2018-01-13T00:36:33+01:00
	[INFO] Final Memory: 11M/309M
	[INFO] ------------------------------------------------------------------------

The log will show you the result of the discovery which contains the unique ID of the device and its location (IP address).

***CAUTION***: If you don't see any devices although they are on the same network as your computer you are scanning with,
please make sure that the devices have LAN-mode switched on!
 
## Built With

Build tools:

* [Maven](https://maven.apache.org/) - Dependency Management
* [Lombok](https://projectlombok.org/) - Reducing Boilerplate Code with Project Lombok

Dependencies:

* [Slf4j](https://www.slf4j.org/) - Simple Logging Facade for Java

and some test dependencies...

## Contributing

tbd.

## Versioning

tbd.

## Authors

* **Wladislaw Mitzel** - *Initial work* - [wlami](https://github.com/wlami)

[//]: # (See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Acknowledgments

* Yeelink for building nice and afforable smart lights.