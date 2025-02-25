package cn.gsq.ssh.config.Interceptor;

import cn.gsq.ssh.config.AbstractSshManager;
import cn.gsq.ssh.config.SocketType;
import cn.gsq.ssh.config.SshAutoConfigure;
import cn.gsq.ssh.jsch.SshBootstrap;
import cn.gsq.ssh.model.SshModel;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.Interceptor.SshWebSocketInterceptor
 *
 * @author : gsq
 * @since : 2022-06-14 14:48
 **/
@Slf4j
public class SshWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler handler, Map<String, Object> attributes) {
        boolean flag = false;
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = serverHttpRequest.getServletRequest();
            String type = httpServletRequest.getParameter("type");
            SocketType socketType = EnumUtil.fromString(SocketType.class, type, null);
            if(ObjectUtil.isNotNull(socketType)) {
                switch (socketType) {
                    case ssh: {
                        flag = sshHandle(httpServletRequest, attributes);
                        break;
                    }
                    default: {
                        log.error("socket类型无匹配项目：" + type);
                    }
                }
                // 传入客户端IP
                attributes.put("ip", ServletUtil.getClientIP(httpServletRequest));
            }
        }
        return flag;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Exception e) {
        if(ObjectUtil.isNotNull(e)) {
            log.error("websocket建立握手错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @Description : ssh整理数据
     * @Param : [request, attributes]
     * @Return : boolean
     * @Author : gsq
     * @Date : 3:28 下午
     * @note : An art cell !
    **/
    private boolean sshHandle(HttpServletRequest request, Map<String, Object> attributes) {
        boolean flag = false;
        String id = request.getParameter("id");
        if(!StrUtil.isBlank(id)) {
            AbstractSshManager manager = SshAutoConfigure.getApplicationContext().getBean(AbstractSshManager.class);
            SshModel model = manager.getSshModelById(id);
            if(ObjectUtil.isNotNull(model)) {
                attributes.put("ssh", model);
                flag = true;
            } else {
                log.error("未找到ssh模型id相对应的执行器：" + id);
            }
        } else {
            log.error("ssh模型id不能为空");
        }
        return flag;
    }

}
