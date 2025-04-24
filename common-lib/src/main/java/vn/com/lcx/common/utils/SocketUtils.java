package vn.com.lcx.common.utils;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

@NoArgsConstructor
public class SocketUtils {

    public String sendAndReceive(String socketHost, int socketPort, String inputMessage) {
        var result = CommonConstant.EMPTY_STRING;
        if (StringUtils.isBlank(socketHost)) {
            throw new NullPointerException("socketHost is blank");
        }
        if (socketPort <= 0) {
            throw new NullPointerException("socketPort is blank");
        }
        if (StringUtils.isBlank(inputMessage)) {
            throw new NullPointerException("inputMessage is blank");
        }
        try (
                Socket socket = new Socket(socketHost, socketPort);
                var output = socket.getOutputStream();
                // var writer = new PrintWriter(output, true);
                var writer = new BufferedWriter(new OutputStreamWriter(output));

                var input = socket.getInputStream();
                var reader = new BufferedReader(new InputStreamReader(input))

        ) {
            LogUtils.writeLog2(LogUtils.Level.INFO, "Connected to the socket server {}:{}", socketHost, socketPort);
            LogUtils.writeLog2(LogUtils.Level.INFO, "Input message: {}", inputMessage);
            // writer.println(inputMessage);
            writer.write(inputMessage);
            writer.flush();

            result = reader.readLine();
            LogUtils.writeLog2(LogUtils.Level.INFO, "Server responded: {}", result);

        } catch (IOException ex) {
            LogUtils.writeLog2(ex.getMessage(), ex);
        }
        return result;
    }

}
