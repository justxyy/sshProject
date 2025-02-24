package cn.gsq.ssh.config;

import cn.gsq.ssh.SshConstant;
import cn.gsq.ssh.config.util.SocketSessionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.BaseHandler
 *
 * @author : gsq
 * @date : 2022-06-14 16:21
 * @note : It's not technology, it's art !
 **/
@Slf4j
public abstract class BaseHandler extends TextWebSocketHandler {

    /**
     * @Description : http握手后执行的函数
     * @Param : [session]
     * @Return : void
     * @Author : gsq
     * @Date : 5:26 下午
     * @note : An art cell !
    **/
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        // 展示欢迎界面banner
        this.showHelloMsg(attributes, session);
        // 根据权限判断是否可用此功能
        String permission = (String) attributes.get("permission");
        if (StrUtil.isNotEmpty(permission)) {
            this.sendMsg(session, permission);
            this.destroy(session);
            return;
        }
        this.afterConnectionEstablishedImpl(session);
    }

    /**
     * @Description : 欢迎界面
     * @Param : [attributes, session]
     * @Return : void
     * @Author : gsq
     * @Date : 5:26 下午
     * @note : An art cell !
    **/
    protected void showHelloMsg(Map<String, Object> attributes, WebSocketSession session) {
        // 随机选择banner
        ClassPathResource resource = new ClassPathResource(
                StrUtil.format(SshConstant.SSH_BANNER_IMG, RandomUtil.randomInt(1, 26))
        );
        String banner = IoUtil.readUtf8(resource.getStream());
        this.sendMsg(session, banner);
    }

    /**
     * @Description : 握手异常
     * @Param : [session, exception]
     * @Return : void
     * @Author : gsq
     * @Date : 5:26 下午
     * @note : An art cell !
    **/
    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) {
        log.error(SshConstant.SSH_EXCEPTION_WEBSOCKET, session.getId(), e.getMessage());
        e.printStackTrace();
        destroy(session);
    }

    /**
     * @Description : 关闭链接后执行函数
     * @Param : [session, status]
     * @Return : void
     * @Author : gsq
     * @Date : 5:28 下午
     * @note : An art cell !
    **/
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        destroy(session);
    }

    /**
     * @Description : 发送消息
     * @Param : [session, msg]
     * @Return : void
     * @Author : gsq
     * @Date : 4:40 下午
     * @note : An art cell !
    **/
    protected void sendMsg(WebSocketSession session, String msg) {
        try {
            SocketSessionUtil.send(session, msg);
        } catch (Exception e) {
            log.error("websocket发送消息尝试10次后失败");
        }
    }

    /**
     * @Description : 会话建立后
     * @Param : [session]
     * @Return : void
     * @Author : gsq
     * @Date : 4:59 下午
     * @note : ⚠️ http握手后的数据已经存入session!
    **/
    protected abstract void afterConnectionEstablishedImpl(WebSocketSession session);

    /**
     * @Description : 关闭链接
     * @Param : [session]
     * @Return : void
     * @Author : gsq
     * @Date : 4:39 下午
     * @note : An art cell !
    **/
    public abstract void destroy(WebSocketSession session);

}
