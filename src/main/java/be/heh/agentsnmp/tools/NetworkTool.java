package be.heh.agentsnmp.tools;

import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkTool {
    private static boolean isPrivateIP(String ipAddress) {
        String[] privateIPRanges = {
                "10.0.0.0/8",
                "172.16.0.0/12",
                "192.168.0.0/16"
        };
        try{
            for (String range : privateIPRanges) {
                SubnetUtils utils = new SubnetUtils(range);
                SubnetUtils.SubnetInfo info = utils.getInfo();
                if (info.isInRange(ipAddress)) {
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            System.out.println("Error can't confirm is private address : "+e.getMessage());
            return false;
        }
    }
    public static List<String> getPrivateIPV4Addresses() throws SocketException {
        try{
            List<String> privateIPs = new ArrayList<>();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof java.net.Inet4Address && isPrivateIP(inetAddress.getHostAddress())) {
                        privateIPs.add(inetAddress.getHostAddress());
                    }
                }
            }

            return privateIPs;
        }catch (SocketException e){
            System.out.println("Error get private address : "+e.getMessage());
            return new ArrayList<>();
        }
    }
}
