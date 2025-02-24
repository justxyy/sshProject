package cn.gsq.ssh.engine;

import cn.gsq.ssh.model.SshModel;

import java.util.List;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.engine.SshObjectEngine
 *
 * @author : gsq
 * @date : 2022-05-11 17:19
 * @note : It's not technology, it's art !
 **/
public interface SshObjectEngine {

    /**
     * @Description : 启动加载ssh实例列表
     * @Param : []
     * @Return : java.util.List<cn.gsq.ssh.model.SshModel>
     * @Author : gsq
     * @Date : 5:45 下午
     * @note : An art cell !
    **/
    List<SshModel> loadingSshModels();

    /**
     * @Description : 实例信息
     * @Param : [model]
     * @Return : void
     * @Author : gsq
     * @Date : 5:45 下午
     * @note : An art cell !
    **/
    void updateSshModel(SshModel model);

    /**
     * @Description : 删除ssh实例
     * @Param : [model]
     * @Return : void
     * @Author : gsq
     * @Date : 5:45 下午
     * @note : An art cell !
    **/
    void deleteSshModel(String id);

}
