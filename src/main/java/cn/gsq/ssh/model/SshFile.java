package cn.gsq.ssh.model;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.model.SshFile
 *
 * @author : gsq
 * @date : 2022-06-08 17:22
 * @note : It's not technology, it's art !
 **/
@Getter
@AllArgsConstructor
public abstract class SshFile {

    private final String name;    // 文件名称

    private final String path;  // 自己相对于父级的路径（绝对路径去掉根目录）

    private final String modifyTime;  // 文件修改时间

    public abstract boolean isDirectory();

    /**
     * @Description : 转化为json string
     * @Param : []
     * @Return : java.lang.String
     * @Author : gsq
     * @Date : 6:03 下午
     * @note : An art cell !
    **/
    public String toJsonString() {
        return JSONUtil.toJsonStr(this);
    }

}
