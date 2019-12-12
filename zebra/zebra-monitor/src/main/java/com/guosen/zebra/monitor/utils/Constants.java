package com.guosen.zebra.monitor.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Constants {

    /**
     * The instance IP.
     */
    private static String instanceIP;
    @SuppressWarnings("rawtypes")
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {

        InetAddress candidateAddress = null;
        // 遍历所有的网络接口
        try {
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }

        if (candidateAddress != null) {
            return candidateAddress;
        }
        // 如果没有发现 non-loopback地址.只能用最次选的方案
        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
        if (jdkSuppliedAddress == null) {
            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
        }
        return jdkSuppliedAddress;
    }

    /**
     * Get instance IP.
     * @return IP of instance.
     */
    public static String getInstanceIP() {
        // Not ThreadSafe, but it doesn't matter.
        if (null == instanceIP || "".equals(instanceIP)) {
            InetAddress ip;
            try {
                ip = getLocalHostLANAddress();
                instanceIP = ip.getHostAddress();
            } catch (UnknownHostException e) {
                instanceIP = "unknown";
            } catch (Throwable t) {
                instanceIP = "unknown";
            }
        }
        return instanceIP;
    }
}
