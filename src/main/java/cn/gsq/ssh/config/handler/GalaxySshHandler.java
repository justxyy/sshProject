package cn.gsq.ssh.config.handler;

import cn.gsq.ssh.SshConstant;
import cn.gsq.ssh.config.BaseTerminalHandler;
import cn.gsq.ssh.jsch.SshBootstrap;
import cn.gsq.ssh.model.SshModel;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.ChannelType;
import cn.hutool.extra.ssh.JschUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.handler.GalaxySshHandler
 *
 * @author : gsq
 * @date : 2022-06-14 15:57
 * @note : It's not technology, it's art !
 **/
@Slf4j
public class GalaxySshHandler extends BaseTerminalHandler {

    private static final ConcurrentHashMap<String, SshInfoTransfer> transfers = new ConcurrentHashMap<>();

    /**
     * @Description : 握手成功后触发函数
     * @Param : [session]
     * @Return : void
     * @Author : gsq
     * @Date : 4:57 下午
     * @note : An art cell !
    **/
    @Override
    protected void afterConnectionEstablishedImpl(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        SshModel model = (SshModel) attributes.get("ssh");
        try {
            SshInfoTransfer transfer = new SshInfoTransfer(session ,model);
            transfer.startReadSshMsg();     // 开启新线程
            transfers.put(session.getId(), transfer);
            ThreadUtil.safeSleep(1000);
        } catch (IOException | JSchException e) {
            log.error(SshConstant.SSH_EXCEPTION_STREAM, model.getId(), e.getMessage());
            e.printStackTrace();
            sendBinary(session, "ssh链接故障");
            this.destroy(session);
        }
    }

    /**
     * @Description : 处理client信息流
     * @Param : [session, message]
     * @Return : void
     * @Author : gsq
     * @Date : 5:04 下午
     * @note : An art cell !
    **/
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        SshInfoTransfer transfer = transfers.get(session.getId());
        if (ObjectUtil.isNull(transfer)) {
            sendBinary(session, "ssh服务链接已经关闭");
            IoUtil.close(session);
            return;
        }
        String payload = message.getPayload();
        if (JSONValidator.from(payload).getType() == JSONValidator.Type.Object) {
            JSONObject jsonObject = JSONObject.parseObject(payload);
            String data = jsonObject.getString("data");
            if (StrUtil.equals(data, "heart-beat")) {
                // 心跳消息不转发
                return;
            }
            if (StrUtil.equals(data, "resize")) {
                // 缓存区大小
                transfer.resize(jsonObject);
                return;
            }
        }
        try {
            transfer.write(payload);
        } catch (IOException e) {
            sendBinary(session, StrUtil.format(SshConstant.SSH_EXCEPTION_WRITE, e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * @Description : 销毁会话
     * @Param : [session]
     * @Return : void
     * @Author : gsq
     * @Date : 5:04 下午
     * @note : An art cell !
    **/
    @Override
    public void destroy(WebSocketSession session) {
        SshInfoTransfer transfer = transfers.get(session.getId());
        if (ObjectUtil.isNotNull(transfer)) {
            transfer.closeAll();
        }
        transfers.remove(session.getId());
    }

    private class SshInfoTransfer extends SshBootstrap {

        private final WebSocketSession webSocketSession;     // 客户端session
        private final Session sshSession;           // ssh会话
        private final ChannelShell channel;         // ssh信息流通讯管道
        private final InputStream inputStream;      // ssh服务主机返回信息流
        private final OutputStream outputStream;    // websocket客户端命令键入信息流
        private final StringBuilder nowLineInput = new StringBuilder(); // 命令缓存

        /**
         * @param model
         * @Description : 构造函数
         * @Param : [model]
         * @Return :
         * @Author : gsq
         * @Date : 6:05 下午
         * @note : An art cell !
         */
        private SshInfoTransfer(WebSocketSession session, SshModel model) throws IOException {
            super(model);
            this.webSocketSession = session;
            this.sshSession = getSshSesion();
            this.channel = (ChannelShell) JschUtil.createChannel(this.sshSession, ChannelType.SHELL);
            this.inputStream = channel.getInputStream();
            this.outputStream = channel.getOutputStream();
        }

        /**
         * @Description : 讲信息写入ssh通讯管道
         * @Param : [msg]
         * @Return : void
         * @Author : gsq
         * @Date : 5:33 下午
         * @note : ⚠️ 该方法会叠加上一次的数据 !
        **/
        private void write(String character) throws IOException {
            if(isAllow(character)) {
                this.outputStream.write(character.getBytes());
            } else {
                sendBinary(this.webSocketSession, SshConstant.SSH_WARN_PERMISSIONS);
                this.outputStream.write(new byte[]{3});     // control + c
            }
            this.outputStream.flush();
        }

        /**
         * @Description : 检查命令是否可以执行
         * @Param : [msg]
         * @Return : boolean
         * @Author : gsq
         * @Date : 2:39 下午
         * @note : ⚠️ 只有键入回车符的时候开始检查 !
        **/
        public boolean isAllow(String msg) {
            boolean refuse = true;
            if (StrUtil.equals(msg, StrUtil.CR)) {
                // 命令将要提交
                String command = nowLineInput.toString();
                String commandHead = CollUtil.getFirst(StrUtil.splitTrim(command, StrUtil.C_SPACE));
                if(CollUtil.contains(getModel().getForbidList(), commandHead)) {
                    refuse = false;
                }
                // 清空命令缓存
                nowLineInput.setLength(0);
            } else {
                // 命令提示符不添加
                if(!StrUtil.equals(msg, StrUtil.TAB)) {
                    this.append(msg);   // 滚动添加命令字符
                }
            }
            return refuse;
        }

        /**
         * @Description : 调整缓存区大小
         * @Param : [jsonObject]
         * @Return : void
         * @Author : gsq
         * @Date : 5:26 下午
         * @note : An art cell !
        **/
        private void resize(JSONObject jsonObject) {
            Integer rows = Convert.toInt(jsonObject.getString("rows"), 10);
            Integer cols = Convert.toInt(jsonObject.getString("cols"), 10);
            Integer wp = Convert.toInt(jsonObject.getString("wp"), 10);
            Integer hp = Convert.toInt(jsonObject.getString("hp"), 10);
            this.channel.setPtySize(cols, rows, wp, hp);
        }

        /**
         * @Description : 写入缓存
         * @Param : [msg]
         * @Return : java.lang.String
         * @Author : gsq
         * @Date : 5:47 下午
         * @note : An art cell !
        **/
        private String append(String msg) {
            char[] x = msg.toCharArray();
            if (x.length == 1 && x[0] == 127) {
                // 退格键
                int length = nowLineInput.length();
                if (length > 0) {
                    nowLineInput.delete(length - 1, length);
                }
            } else {
                nowLineInput.append(msg);
            }
            return nowLineInput.toString();
        }

        /**
         * @Description : 循环等待ssh信息
         * @Param : []
         * @Return : void
         * @Author : gsq
         * @Date : 4:45 下午
         * @note : ⚠️ 此方法会新开启线程 !
         **/
        private void startReadSshMsg() throws JSchException {
            this.channel.connect();
            ThreadUtil.execute(this::blockRead);
        }

        /**
         * @Description : 阻塞等待ssh返回信息
         * @Param : []
         * @Return : void
         * @Author : gsq
         * @Date : 4:46 下午
         * @note : ⚠️ 此方法会导致线程阻塞 !
        **/
        private void blockRead() {
            try {
                byte[] buffer = new byte[1024];
                int i;
                // 阻塞等待ssh管道数据返回
                while ((i = inputStream.read(buffer)) != -1) {
                    // control + c 取消标示不返回
                    String answer = new String(Arrays.copyOfRange(buffer, 0, i), this.model.getCharset());
                    sendBinary(
                            webSocketSession,
                            answer.replace("^C", "")
                    );
                }
            } catch (Exception e) {
                log.error(SshConstant.SSH_EXCEPTION_MONITOR, e.getMessage());
                e.printStackTrace();
                if (!this.sshSession.isConnected()) {
                    return;
                }
                GalaxySshHandler.this.destroy(this.webSocketSession);
            }
        }

        /**
         * @Description : 关闭所有链接
         * @Param : []
         * @Return : void
         * @Author : gsq
         * @Date : 4:53 下午
         * @note : An art cell !
        **/
        private void closeAll() {
            IoUtil.close(this.inputStream);
            IoUtil.close(this.outputStream);
            JschUtil.close(this.channel);
            JschUtil.close(this.sshSession);
            IoUtil.close(this.webSocketSession);
        }

    }

}
