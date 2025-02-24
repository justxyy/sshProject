package cn.gsq.ssh.config;

import cn.gsq.ssh.SshConstant;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.BaseTerminalHandler
 *
 * @author : gsq
 * @date : 2022-06-14 16:20
 * @note : It's not technology, it's art !
 **/
@Slf4j
public abstract class BaseTerminalHandler extends BaseHandler {

    /**
     * @Description : websocket向客户端发送消息
     * @Param : [session, msg]
     * @Return : void
     * @Author : gsq
     * @Date : 4:36 下午
     * @note : An art cell !
    **/
    protected void sendBinary(WebSocketSession session, String msg) {
        if (StrUtil.isEmpty(msg)) {
            return;
        }
        if (!session.isOpen()) {
            log.warn(SshConstant.SSH_WARN_SEND, msg);
            return;
        }
        synchronized (session.getId()) {
            BinaryMessage byteBuffer = new BinaryMessage(msg.getBytes());
            try {
                session.sendMessage(byteBuffer);
            } catch (IOException e) {
                log.error(SshConstant.SSH_EXCEPTION_SEND, e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
