package com.guosen.zebra.core.grpc.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guosen.zebra.core.conf.HostPort;

public class NetUtils {

  private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

  public static final String LOCALHOST = "127.0.0.1";

  public static final String ANYHOST = "0.0.0.0";

  private static final int RND_PORT_START = 30000;

  private static final int RND_PORT_RANGE = 10000;

  private static final Random RANDOM = new Random(System.currentTimeMillis());
  
  public static String localIp=null;

  public static int getRandomPort() {
    return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
  }

  public static int getAvailablePort() {
    ServerSocket ss = null;
    try {
      ss = new ServerSocket();
      ss.bind(null);
      return ss.getLocalPort();
    } catch (IOException e) {
      return getRandomPort();
    } finally {
      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public static int getAvailablePort(int port) {
    if (port <= 0) {
      return getAvailablePort();
    }
    for (int i = port; i < MAX_PORT; i++) {
      ServerSocket ss = null;
      try {
        ss = new ServerSocket(i);
        return i;
      } catch (IOException e) {
        // continue
      } finally {
        if (ss != null) {
          try {
            ss.close();
          } catch (IOException e) {
          }
        }
      }
    }
    return port;
  }

  private static final int MIN_PORT = 0;

  private static final int MAX_PORT = 65535;

  public static boolean isInvalidPort(int port) {
    return port > MIN_PORT || port <= MAX_PORT;
  }

  private static final Pattern ADDRESS_PATTERN =
      Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");

  public static boolean isValidAddress(String address) {
    return ADDRESS_PATTERN.matcher(address).matches();
  }

  private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

  public static boolean isLocalHost(String host) {
    return StringUtils.isNotBlank(host)
        && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase("localhost"));
  }

  public static boolean isAnyHost(String host) {
    return "0.0.0.0".equals(host);
  }

  public static boolean isInvalidLocalHost(String host) {
    return host == null || host.length() == 0 || host.equalsIgnoreCase("localhost")
        || host.equals("0.0.0.0") || (LOCAL_IP_PATTERN.matcher(host).matches());
  }

  public static boolean isValidLocalHost(String host) {
    return !isInvalidLocalHost(host);
  }

  public static InetSocketAddress getLocalSocketAddress(String host, int port) {
    return isInvalidLocalHost(host) ? new InetSocketAddress(port)
        : new InetSocketAddress(host, port);
  }

  private static final Pattern IP_PATTERN = Pattern.compile(
      "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");

  private static boolean isValidAddress(InetAddress address) {
    if (address == null || address.isLoopbackAddress())
      return false;
    String name = address.getHostAddress();
    return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && isIP(name));
  }

  public static boolean isIP(String addr) {
    if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
      return false;
    }
    return IP_PATTERN.matcher(addr).find();
  }

  public static String getLocalHost() {
	  if(localIp ==null){
		  InetAddress address = getLocalAddress();
		  localIp=( address == null ? LOCALHOST : address.getHostAddress());
	  }
	  return localIp;
  }

	public static String filterLocalHost(String host) {
		if (host == null || host.length() == 0) {
			return host;
		}
		if (host.contains("://")) {
			HostPort u = valueOf(host);
			if (NetUtils.isInvalidLocalHost(u.getHost())) {
				return NetUtils.getLocalHost();
			}
		} else if (host.contains(":")) {
			int i = host.lastIndexOf(':');
			if (NetUtils.isInvalidLocalHost(host.substring(0, i))) {
				return NetUtils.getLocalHost() + host.substring(i);
			}
		} else {
			if (NetUtils.isInvalidLocalHost(host)) {
				return NetUtils.getLocalHost();
			}
		}
		return host;
	}

  private static volatile InetAddress LOCAL_ADDRESS = null;

  public static InetAddress getLocalAddress() {
    if (LOCAL_ADDRESS != null)
      return LOCAL_ADDRESS;
    InetAddress localAddress = getLocalAddress0();
    LOCAL_ADDRESS = localAddress;
    return localAddress;
  }

  public static String getLogHost() {
    InetAddress address = LOCAL_ADDRESS;
    return address == null ? LOCALHOST : address.getHostAddress();
  }

  private static InetAddress getLocalAddress0() {
    InetAddress result = null;
    try {
      int lowest = Integer.MAX_VALUE;
      for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
          .hasMoreElements();) {
        NetworkInterface ifc = nics.nextElement();
        if (ifc.isUp()) {
          logger.trace("Testing interface: " + ifc.getDisplayName());
          if (ifc.getIndex() < lowest || result == null) {
            lowest = ifc.getIndex();
          } else if (result != null) {
            continue;
          }
          for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements();) {
            InetAddress address = addrs.nextElement();
            if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
              logger.trace("Found non-loopback interface: " + ifc.getDisplayName());
              result = address;
            }
          }

        }
      }
    } catch (IOException ex) {
      logger.error("Cannot get first non-loopback address", ex);
    }
    if (result != null && isValidAddress(result)) {
      return result;
    }
    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      logger.warn("Unable to retrieve localhost");
    }
    return null;
  }

  public static String getIpByHost(String hostName) {
    try {
      return InetAddress.getByName(hostName).getHostAddress();
    } catch (UnknownHostException e) {
      return hostName;
    }
  }

  public static String toAddressString(InetSocketAddress address) {
    return address.getAddress().getHostAddress() + ":" + address.getPort();
  }

  public static InetSocketAddress toAddress(String address) {
    int i = address.indexOf(':');
    String host;
    int port;
    if (i > -1) {
      host = address.substring(0, i);
      port = Integer.parseInt(address.substring(i + 1));
    } else {
      host = address;
      port = 0;
    }
    return new InetSocketAddress(host, port);
  }

  public static String toURL(String protocol, String host, int port, String path) {
    StringBuilder sb = new StringBuilder();
    sb.append(protocol).append("://");
    sb.append(host).append(':').append(port);
    if (path.charAt(0) != '/')
      sb.append('/');
    sb.append(path);
    return sb.toString();
  }

  public static HostPort valueOf(String url) {
      if (url == null || (url = url.trim()).length() == 0) {
          throw new IllegalArgumentException("url == null");
      }
      String host = null;
      int port = 0;
      String path = null;
      Map<String, String> parameters = null;
      int i = url.indexOf("?"); // seperator between body and parameters
      if (i >= 0) {
          String[] parts = url.substring(i + 1).split("\\&");
          parameters = new HashMap<String, String>();
          for (String part : parts) {
              part = part.trim();
              if (part.length() > 0) {
                  int j = part.indexOf('=');
                  if (j >= 0) {
                      parameters.put(part.substring(0, j), part.substring(j + 1));
                  } else {
                      parameters.put(part, part);
                  }
              }
          }
          url = url.substring(0, i);
      }
      i = url.indexOf("/");
      if (i >= 0) {
          path = url.substring(i + 1);
          url = url.substring(0, i);
      }
      i = url.indexOf(":");
      if (i >= 0 && i < url.length() - 1) {
          port = Integer.parseInt(url.substring(i + 1));
          url = url.substring(0, i);
      }
      if (url.length() > 0) host = url;
      return new HostPort(host, port, path, parameters);
  }
}
