package cn.gsq.ssh.config.util;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.util.SocketSessionUtil
 *
 * @author : gsq
 * @date : 2022-06-14 16:24
 * @note : It's not technology, it's art !
 **/
@Slf4j
public class SocketSessionUtil {

    private static final KeyLock<String> LOCK = new KeyLock<>();    // 锁🔒

    private static final int ERROR_TRY_COUNT = 10;  // 失败次数

    /**
     * @Description : 使用session发送信息
     * @Param : [session, msg]
     * @Return : void
     * @Author : gsq
     * @Date : 4:43 下午
     * @note : An art cell !
    **/
    public static void send(final Session session, String msg) throws IOException {
        if (StrUtil.isEmpty(msg)) {
            return;
        }
        if (!session.isOpen()) {
            throw new RuntimeException("会话已关闭");
        }
        try {
            LOCK.lock(session.getId());
            IOException exception = null;
            int tryCount = 0;
            do {
                tryCount++;
                if (exception != null) {
                    // 上一次有异常、休眠 500
                    ThreadUtil.sleep(500);
                }
                try {
                    session.getBasicRemote().sendText(msg);
                    exception = null;
                    break;
                } catch (IOException e) {
                    log.error("websocket发送消息失败：{} 次", tryCount);
                    exception = e;
                    e.printStackTrace();
                }
            } while (tryCount <= ERROR_TRY_COUNT);
            if (exception != null) {
                throw exception;
            }
        } finally {
            LOCK.unlock(session.getId());
        }
    }

    /**
     * @Description : 使用WebSocketSession发送消息
     * @Param : [session, msg]
     * @Return : void
     * @Author : gsq
     * @Date : 4:45 下午
     * @note : An art cell !
    **/
    public static void send(WebSocketSession session, String msg) throws IOException {
        if (StrUtil.isEmpty(msg)) {
            return;
        }
        if (!session.isOpen()) {
            throw new RuntimeException("会话已关闭");
        }
        try {
            LOCK.lock(session.getId());
            IOException exception = null;
            int tryCount = 0;
            do {
                tryCount++;
                if (exception != null) {
                    // 上一次有异常、休眠 500
                    ThreadUtil.sleep(500);
                }
                try {
                    session.sendMessage(new TextMessage(msg));
                    exception = null;
                    break;
                } catch (IOException e) {
                    log.error("websocket发送消息失败：{} 次", tryCount);
                    exception = e;
                    e.printStackTrace();
                }
            } while (tryCount <= ERROR_TRY_COUNT);
            if (exception != null) {
                throw exception;
            }
        } finally {
            LOCK.unlock(session.getId());
        }
    }

}
