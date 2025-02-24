package cn.gsq.ssh.config;

import cn.gsq.ssh.SshConstant;
import cn.gsq.ssh.config.Interceptor.SshWebSocketInterceptor;
import cn.gsq.ssh.config.handler.GalaxySshHandler;
import cn.gsq.ssh.engine.SshObjectEngine;
import cn.gsq.ssh.model.SshModel;
import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.SshConfigure
 *
 * @author : gsq
 * @date : 2022-05-11 17:16
 * @note : It's not technology, it's art !
 **/
@Configuration
@EnableWebSocket
@EnableConfigurationProperties(SshProperties.class)
public class SshAutoConfigure implements WebSocketConfigurer, ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        setApplicationContexts(context);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private static synchronized void setApplicationContexts(ApplicationContext applicationContext) {
        SshAutoConfigure.applicationContext = applicationContext;
    }

    /**
     * @Description : 获取ssh实例manager
     * @Param : []
     * @Return : cn.gsq.ssh.config.AbstractSshManager
     * @Author : gsq
     * @Date : 5:28 下午
     * @note : An art cell !
    **/
    @Bean("sshManager")
    @ConditionalOnMissingBean(AbstractSshManager.class)
    public AbstractSshManager getAbstractSshManager() {
        return new AbstractSshManager() {};
    }

    /**
     * @Description : 获取ssh实例加载引擎
     * @Param : []
     * @Return : cn.gsq.ssh.engine.SshObjectEngine
     * @Author : gsq
     * @Date : 5:29 下午
     * @note : An art cell !
    **/
    @Bean("sshObjectEngine")
    @ConditionalOnMissingBean(SshObjectEngine.class)
    public SshObjectEngine getSshObjectEngine() {
        return new SshObjectEngine() {
            @Override
            public List<SshModel> loadingSshModels() {
                return CollUtil.newArrayList();
            }

            @Override
            public void updateSshModel(SshModel model) {

            }

            @Override
            public void deleteSshModel(String id) {

            }
        };
    }

    /**
     * @Description : 启动加载实例列表
     * @Param : [manager]
     * @Return : javafx.util.Pair<java.lang.String,java.lang.String>
     * @Author : gsq
     * @Date : 5:48 下午
     * @note : ⚠️ 只为了加载 !
    **/
    @Bean(name = "SshManagerLoad")
    public Pair<String, String> load(AbstractSshManager manager) {
        manager.loadingSshModels();
        return null;
    }

    /**
     * @Description : 注册websocket端口返回命令执行信息
     * @Param : [registry]
     * @Return : void
     * @Author : gsq
     * @Date : 5:19 下午
     * @note : An art cell !
    **/
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        SshWebSocketInterceptor interceptor = new SshWebSocketInterceptor();
        // 注册ssh窗口链接socket
        registry.addHandler(new GalaxySshHandler(), SshConstant.SSH_SOCKET_ENDPOINT)
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }

}
