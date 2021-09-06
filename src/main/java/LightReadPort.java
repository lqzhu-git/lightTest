import jssc.*;

import java.io.ByteArrayOutputStream;

/**
 * @author EFL-tjl
 */
public class LightReadPort implements SerialPortEventListener {

    /**
     * 查询ID
     */
    private final byte[] CHECK = {(byte) 0xAA, 1, 0, 1};
    /**
     * 查询光强
     */
    private final byte[] READ = {(byte) 0xAA, 2, 0, 0};

    private StringBuilder receiveStore = new StringBuilder();
    private static final int WAIT = 0, OK = 1, ERROR = 2;
    private volatile int state;
    private SerialPort serialPort;

    private volatile double intensity = 0;
    private String strIntensity;

    public boolean connect() {
        String[] comList = SerialPortList.getPortNames("COM");
        if (comList == null) {
            serialPort = null;
            return false;
        }
        for (String portName : comList) {
            if ("COM1".equals(portName)) {
                continue;
            }
            if (checkPort(portName)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPort(String portName) {
        if (portName == null || portName.matches("\\s*")) {
            return false;
        }
        try {
            System.out.println("光强计控制尝试连接：" + portName);
            serialPort = openPort(portName);
            state = WAIT;
            serialPort.writeBytes(CHECK);
            waitReply();
            if (state == OK) {
                return true;
            }
        } catch (SerialPortException e) {
            System.out.println("光强计控制尝试连接失败，串口：" + portName);
        }
        System.out.println("光强计控制尝试连接错误：" + portName);
        closePort();
        return false;
    }

    public double read() {
        if (serialPort != null) {
            try {
                state = WAIT;
                serialPort.writeBytes(READ);
                waitReply();
            } catch (SerialPortException e) {
                System.out.println("获取光强值错误");
            }
        }
        return intensity;
    }

    private void waitReply() {
        long startTime = System.currentTimeMillis();
        while (state != OK && serialPort != null) {
            if (System.currentTimeMillis() - startTime > 1000) {
                System.out.println("等待超时");
                return;
            }
        }
    }

    private SerialPort openPort(String portName) throws SerialPortException {
        SerialPort port = new SerialPort(portName);
        port.openPort();
        port.setParams(SerialPort.BAUDRATE_38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        port.addEventListener(this, SerialPort.MASK_RXCHAR);
        return port;
    }

    public void closePort() {
        if (serialPort != null) {
            try {
                serialPort.removeEventListener();
                System.out.println(serialPort.getPortName() + "监听已移除");
            } catch (SerialPortException e) {
                System.out.println("移出串口监听失败" + e.toString());
            }
            try {
                serialPort.closePort();
                System.out.println(serialPort.getPortName() + "已关闭");
            } catch (SerialPortException e) {
                System.out.println("关闭串口失败" + e.toString());
            }
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            System.out.println("光强计串口数据事件，长度：" + serialPortEvent.getEventValue());
            String receive;
            try {
                receive = serialPort.readHexString();
                if (receive == null) {
                    return;
                }
            } catch (SerialPortException e) {
                System.out.println("读取光强计串口数据失败" + e.toString());
                // todo
                return;
            }
            receiveStore.append(receive);
            System.out.println("received:" + receiveStore);
            if (receiveStore.length() > 1) {
                String allReceived = receiveStore.toString();
                System.out.println("已收集到可判断数据：" + allReceived);
                receiveStore = new StringBuilder();
                if (allReceived.startsWith("81")) {
                    if (allReceived.length() == 29) {
                        strIntensity = decode(allReceived.substring(6, 23));
                        if (!strIntensity.contains("Over")) {
                            intensity = Double.parseDouble(strIntensity);
                        }
                    }
                    state = OK;
                } else {
                    state = ERROR;
                }
            }
        }
    }

    private static final String HEX_STRING = "0123456789ABCDEF";
    public static String decode(String bytes) {
        bytes = bytes.replaceAll("\\s", "");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2) {
            baos.write((HEX_STRING.indexOf(bytes.charAt(i)) << 4 | HEX_STRING.indexOf(bytes.charAt(i + 1))));
        }
        return new String(baos.toByteArray());
    }
}
