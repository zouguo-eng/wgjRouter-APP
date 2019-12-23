package com.example.consts;

public final class Consts {
    public static final String NET_HOST_HOLL = "http://192.168.80.1";
    public static final String ROOT_URL_HOLL = "smb://root:holl0311@192.168.80.1/holl/";//固定
    public static final String CANONI_URL_HOLL = "smb://root:holl0311@192.168.80.1/holl/";//随着目录的进入和回退而变化

    /**
     *
     * smbpasswd -a root新增root用户配置密码
     */

    public static final String TELNETHOST = "192.168.80.1";
    public static final int TELNETPORT = 23;
    public static final String SOCKETHOST = "192.168.80.1";
    public static final int SOCKETPORT = 7888;

    //Tcp通信协议如下
    public static final String ROUTER_BEATS = "<?xml version=\"1.0\"?><CMD_REQ code=\"11\" index=\"1\"/>";
    public static final String ROUTER_SET_WIFI = "<?xml version=\"1.0\"?><CMD_REQ code=\"2\" index=\"2\"><SSID>**</SSID><PASSWORD>&&</PASSWORD><HIDDEN>0</HIDDEN></CMD_REQ>";
    //扫描周围AP信息
    public static final String ROUTER_SCAN_AP = "<?xml version=\"1.0\"?><CMD_REQ code=\"5\" index=\"3\"/>";
    //设置无线中继--（无效）
    @Deprecated
    public static final String ROUTER_SET_REPEATER = "<?xml version=\"1.0\"?><CMD_REQ code=\"6\" index=\"4\"><SSID>LEDE</SSID><PASSWORD>w12345679</PASSWORD></CMD_REQ>";
    //获取中继的无线SSID
    public static final String ROUTER_GET_REPEATER = "<?xml version=\"1.0\"?><CMD_REQ code=\"7\" index=\"5\"/>";
    //获取硬件版本、固件版本
    public static final String ROUTER_GET_VERSION = "<?xml version=\"1.0\"?><CMD_REQ code=\"9\" index=\"6\"/>";
    //获取硬盘、WIFI、MAC信息
    public static final String ROUTER_GET_INFOS = "<?xml version=\"1.0\"?><CMD_REQ code=\"13\" index=\"7\"/>";
    public static final String ROUTER_GET_INFO = "<?xml version=\"1.0\"?><CMD_REQ code=\"14\" index=\"8\"/>";
    //当前连接设备（包含操作权限）
    public static final String ROUTER_ON_DEVICES = "<?xml version=\"1.0\"?><CMD_REQ code=\"647\" index=\"9\"/>";
    //重启、关机--（无效）
    @Deprecated
    public static final String ROUTER_SHUT_OR_REBOOT = "<?xml version=\"1.0\"?><CMD_REQ code=\"500\" index=\"10\"><ACTION>0</ACTION></CMD_REQ>";
    //恢复出厂设置(内部MD5加密)
    public static final String RESTORE_ROUTER_BYSOCKET_FIRST = "<?xml version=\"1.0\"?><CMD_REQ code=\"649\" index=\"11\"><MAC>02:00:00:00:00:00</MAC></CMD_REQ>";
    public static final String RESTORE_ROUTER_BYSOCKET_TWO = "<?xml version=\"1.0\"?><CMD_REQ code=\"650\" index=\"12\"><MDFIVE>21232f297a57a5a743894a0e4a801fc3</MDFIVE></CMD_REQ>";
    public static final String RESTORE_ROUTER_BYSOCKET_THREE = "<?xml version=\"1.0\"?><CMD_REQ code=\"651\" index=\"13\"><MDFIVE>116e8505bdba16773829d490f1de37a0</MDFIVE></CMD_REQ>";
    public static final String RESTORE_ROUTER_BYSOCKET_OVER = "<?xml version=\"1.0\"?><CMD_REQ code=\"500\" index=\"14\"><ACTION>1</ACTION></CMD_REQ>";
    //访问权限控制
    public static final String ROUTER_PHONE_PERMISSION = "<?xml version=\"1.0\"?><CMD_REQ code=\"647\" index=\"15\"/>";

    //Telnet命令操作如下
    public static final String CHANGE_WIFI_SSID = "uci set wireless.@wifi-iface[0].ssid=**";
    public static final String CHANGE_WIFI_KEY = "uci set wireless.@wifi-iface[0].key=**";
    public static final String CHANGE_WIFI_ENCR = "uci set wireless.@wifi-iface[0].encryption=psk-mixed+ccmp";
    public static final String CHANGE_WIFI_COMMIT = "uci commit wireless && wifi &";

    //单独获取WiFi名称、密码
    public static final String GET_WIFI_SSID = "uci get wireless.@wifi-iface[0].ssid";
    public static final String GET_WIFI_KEY = "uci get wireless.@wifi-iface[0].key";

    //开启中继(每次关机后开机、被桥接WIFI消失需要重新开启)
    //配置 wan_wifi 网络接口
    public static final String REPEATER_SET_NETWORK_FIRST = "uci set network.wan_wifi=interface";
    public static final String REPEATER_SET_NETWORK_TWO = "uci set network.wan_wifi.ifname=wlan0";
    public static final String REPEATER_SET_NETWORK_THREE = "uci set network.wan_wifi.proto=dhcp";
    public static final String REPEATER_SET_NETWORK_OVER = "uci commit network";
    //配置 STA 接口,新增时应确保 无线桥接 处于关闭状态
    public static final String REPEATER_SET_WIRELESS_FIRST = "uci add wireless wifi-iface";
    public static final String REPEATER_SET_WIRELESS_TWO = "uci set wireless.@wifi-iface[-1].device=radio0";
    public static final String REPEATER_SET_WIRELESS_THREE = "uci set wireless.@wifi-iface[-1].network=wan_wifi";
    public static final String REPEATER_SET_WIRELESS_FOUR = "uci set wireless.@wifi-iface[-1].mode=sta";
    public static final String REPEATER_SET_WIRELESS_FIVE = "uci set wireless.@wifi-iface[-1].ssid=**";
    public static final String REPEATER_SET_WIRELESS_SIX = "uci set wireless.@wifi-iface[-1].encryption=**";
    public static final String REPEATER_SET_WIRELESS_SEVEN = "uci set wireless.@wifi-iface[-1].key=**";
    public static final String REPEATER_SET_WIRELESS_OVER = "uci commit wireless";
    //配置 apconfig文件，增加当前中继信息
    public static final String REPEATER_SET_APCONFIG_FIRST = "uci add apconfig AP0";
    public static final String REPEATER_SET_APCONFIG_TWO = "uci set apconfig.@AP0[-1].name=**";
    public static final String REPEATER_SET_APCONFIG_THREE = "uci set apconfig.@AP0[-1].key=**";
    //public static final String REPEATER_SET_APCONFIG_FOUR = "uci set apconfig.@AP0[-1].chanel=**";
    public static final String REPEATER_SET_APCONFIG_FIVE = "uci set apconfig.@AP0[-1].encryption=**";
    public static final String REPEATER_SET_APCONFIG_OVER = "uci commit apconfig";
    //配置中继开启标志
    public static final String REPEATER_ON_STASTATUS = "sed -i \"s/Wifi_Route=0/Wifi_Route=1/g\" /etc/config/StaStatus";

    //关闭中继
    public static final String REPEATER_DEL_NETWORK = "uci delete network.wan_wifi";
    public static final String REPEATER_DEL_WIRELESS = "uci delete wireless.@wifi-iface[-1]";
    public static final String REPEATER_DEL_APCONFIG = "uci delete apconfig.@AP0[-1]";
    //配置中继关闭标志
    public static final String REPEATER_OFF_STASTATUS = "sed -i \"s/Wifi_Route=1/Wifi_Route=0/g\" /etc/config/StaStatus";

    //判断中继是否开启，通过检测配置文件,mode为sta，代表中继的配置文件正常
    public static final String REPEATER_CHECK_STATUS = "uci get wireless.@wifi-iface[-1].mode";
    //获取中继开启时的AP名
    public static final String REPEATERED_GET_APNAME = "uci get apconfig.@AP0[-1].name";

    //获取路由器网关
    public static final String GET_ROUTER_GATEWAY = "uci get network.lan.ipaddr";
    //软关机,重新开机后中继正常
    public static final String SHUTDOWN_ROUTER = "reboot";
    //脚本关机，安全stop后关机
    public static final String SHUTDOWN_ROUTER_BYSH = "poweroff.sh";
    //脚本恢复出厂设置
    public static final String RESTORE_ROUTER_BYSH = "reset.sh";

    //网络配置服务生效
    public static final String NETWORK_WIFI_RELAOD = "wifi";
    public static final String NETWORK_SERVICE_REINIT = "/etc/init.d/dnsmasq restart";

    //恢复出厂设置
    public static final String RESTORE_ROUTER_NETWORK = "uci delete network.wan_wifi";  //必须先做开启中继检测
    public static final String RESTORE_ROUTER_WIRELESS = "uci delete wireless.@wifi-iface[-1]";
    public static final String RESTORE_ROUTER_APCONFIG = "uci delete apconfig.@AP0[-1]";
    public static final String RESTORE_ROUTER_STASTATUS = "sed -i \"s/Wifi_Route=1/Wifi_Route=0/g\" /etc/config/StaStatus";
    public static final String RESTORE_ROUTER_WANSTATUS = "sed -i \"s/status=1/status=0/g\" /etc/config/WanStatus";

    //jffs2reset: This will erase all settings and remove any installed packages. Are you sure?
}
