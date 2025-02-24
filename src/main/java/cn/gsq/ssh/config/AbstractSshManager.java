package cn.gsq.ssh.config;

import cn.gsq.ssh.SshConstant;
import cn.gsq.ssh.engine.SshObjectEngine;
import cn.gsq.ssh.jsch.SshBootstrap;
import cn.gsq.ssh.model.SshModel;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.AbstractSshManager
 *
 * @author : gsq
 * @date : 2022-05-11 17:21
 * @note : It's not technology, it's art !
 **/
@Slf4j
public abstract class AbstractSshManager {

    @Autowired
    private SshObjectEngine engine;

    @Autowired
    private SshProperties properties;

    private List<SshBootstrap> bootstraps = CollUtil.newArrayList();

    protected void loadingSshModels() {
        bootstraps.addAll(
                CollUtil.map(
                        CollUtil.map(
                                engine.loadingSshModels(),
                                m -> m.setTemp(
                                        FileUtil.normalize(
                                                properties.getHome() + StrUtil.SLASH + m.getId()
                                        )
                                ),
                                true),
                        SshBootstrap::new,
                        true)
        );
    }

    /**
     * @Description : 更新ssh实例
     * @Param : [model]
     * @Return : void
     * @Author : gsq
     * @Date : 6:15 下午
     * @note : An art cell !
    **/
    public void update(SshModel model) {
        model.setTemp(FileUtil.normalize(properties.getHome() + StrUtil.SLASH + model.getId()));
        SshBootstrap bootstrap  = CollUtil.findOne(this.bootstraps, b -> b.getId().equals(model.getId()));
        if(ObjectUtil.isNotEmpty(bootstrap)) {
            // 删除原有的ssh实例
            CollUtil.removeWithAddIf(this.bootstraps, b -> b.getId().equals(model.getId()));
        }
        // 添加更新的ssh实例
        this.bootstraps.add(new SshBootstrap(model));
        // 执行回调函数
        engine.updateSshModel(model);
    }

    /**
     * @Description : 删除ssh实例
     * @Param : [hostname]
     * @Return : void
     * @Author : gsq
     * @Date : 6:19 下午
     * @note : An art cell !
    **/
    public void delete(String id) {
        SshBootstrap bootstrap  = CollUtil.findOne(this.bootstraps, b -> b.getId().equals(id));
        if(ObjectUtil.isNotEmpty(bootstrap)) {
            // 删除原有的ssh实例
            CollUtil.removeWithAddIf(this.bootstraps, b -> b.getId().equals(id));
            // 执行回调函数
            engine.deleteSshModel(id);
        }
    }

    /**
     * @Description : 展示ssh链接模型列表
     * @Param : []
     * @Return : java.util.List<cn.gsq.ssh.model.SshModel>
     * @Author : gsq
     * @Date : 4:52 下午
     * @note : ⚠️ 已按照主机名排序 !
    **/
    public List<SshModel> list() {
        List<SshModel> models = CollUtil.map(this.bootstraps, SshBootstrap::getModel, true);
        return CollUtil.sort(models, Comparator.comparing(SshModel::getHostname));
    }

    /**
     * @Description : 根据id获取单个ssh链接模型
     * @Param : [id]
     * @Return : cn.gsq.ssh.model.SshModel
     * @Author : gsq
     * @Date : 5:00 下午
     * @note : An art cell !
    **/
    public SshModel getSshModelById(String id) {
        List<SshModel> models = CollUtil.map(this.bootstraps, SshBootstrap::getModel, true);
        return CollUtil.findOne(models, m -> m.getId().equals(id));
    }

    /**
     * @Description : 根据id获取ssh执行器
     * @Param : [id]
     * @Return : cn.gsq.ssh.jsch.SshBootstrap
     * @Author : gsq
     * @Date : 4:57 下午
     * @note : An art cell !
    **/
    public SshBootstrap getSshBootstrapById(String id) {
        return CollUtil.findOne(this.bootstraps, b -> b.getId().equals(id));
    }

    public SshBootstrap getSshBootstrapByHostname(String hostname){
        Collection<SshBootstrap> bootstraps1 = CollUtil.filterNew(this.bootstraps, b -> b.getModel().getHostname().equals(hostname));
        SshBootstrap result = null;
        for (SshBootstrap b : bootstraps1){
            if(isAvailable(b.getModel())){
                result = b;
                break;
            }
        }
        return result;
    }

    /**
     * 将sshmodel加入内存
     * @param model
     */
    public synchronized void addSshModel(SshModel model){
        bootstraps.add(new SshBootstrap(model));
    }

    /**
     * 判断sshmodel是否可用
     * @return
     */
    public boolean isAvailable(SshModel model) {
        boolean flag = true;
        Session session = getSshSesion(model);
        if(ObjectUtil.isNull(session)) {
            flag = false;
        }
        close(session);
        return flag;
    }

    /**
     * @Description : 获取ssh会话
     * @Param : []
     * @Return : com.jcraft.jsch.Session
     * @Author : gsq
     * @Date : 2:22 下午
     * @note : ⚠️ 此时链接已经打开 !
     **/
    protected Session getSshSesion(SshModel model) {
        Session session = null;
        try {
            session = JschUtil.openSession(model.getHostname(), model.getPort(), model.getUser(),
                    model.getPassword(), model.getTimeout());
            session.setServerAliveInterval((int) TimeUnit.SECONDS.toMillis(5));
            session.setServerAliveCountMax(5);
        } catch (Exception e) {
//            log.error(SshConstant.SSH_EXCEPTION_CONNECTION, model.getName());
//            e.printStackTrace();
        }
        return session;
    }

    /**
     * @Description : 关闭ssh会话
     * @Param : [session]
     * @Return : void
     * @Author : gsq
     * @Date : 2:24 下午
     * @note : An art cell !
     **/
    protected void close(Session session) {
        if(ObjectUtil.isNotNull(session)) {
            JschUtil.close(session);
        }
    }

}
