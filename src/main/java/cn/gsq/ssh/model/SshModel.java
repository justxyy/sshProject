package cn.gsq.ssh.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import lombok.experimental.Accessors;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.entity.SshEntity
 *
 * @author : gsq
 * @date : 2022-05-11 17:32
 * @note : It's not technology, it's art !
 **/
@Getter
@Accessors(chain = true)
public class SshModel {

    @NonNull
    private final String id;      // 唯一标识

    @NonNull
    private final String name;    // 连接名称

    @NonNull
    private final String hostname;    // 主机名或ip

    @NonNull
    private final int port;   // ssh协议端口号

    @NonNull
    private final String user;    // 用户名

    @NonNull
    private final String password;    // 密码

    @Setter
    @NonNull
    private String temp;  // 文件临时目录

    private final String charset;     // 编码格式

    private final String forbid;     // 禁止使用的命令（多个用回车符隔开）

    private final String suffix;      // 允许编辑的文件后缀名（多个用回车符隔开）

    private final boolean discover;   // 是否显示隐藏文件

    private final String dirs;    // sftp使用的目录（默认情况根据user计算）；管理的文件目录列表（以回车作为分隔符）

    private final Integer timeout = 10000;    // 链接超时时间（单位毫秒）

    private static final String PATH_SEPARATOR = StrUtil.SLASH;     // 路径分隔符

    @Setter
    @Getter
    private Boolean isAvailable;//是否可用

    /**
     * @Description : 构造函数
     * @Param : [id, name, hostname, port, user, password, forbid, suffix, dirs]
     * @Return :
     * @Author : gsq
     * @Date : 3:48 下午
     * @note : ⚠️ forbid, suffix, dirs 三个参数可以为空 !
    **/
    public SshModel(String id, String name, String hostname, int port, String user, String password,
                    String temp, String charset, String forbid, String suffix, boolean discover, String dirs) {
        this.id = id;
        this.name = name;
        this.hostname = hostname;
        this.port = port;
        this.user = user;
        this.password = password;
        this.temp = temp;
        this.charset = StrUtil.isBlank(charset) ? "UTF-8" : charset;
        this.forbid = StrUtil.isBlank(forbid) ? "reboot" : forbid;
        this.suffix = StrUtil.isBlank(suffix) ? "txt" : suffix;
        this.discover = discover;
        // 空则按用户计算；非空则直接赋值
        this.dirs = StrUtil.isBlank(dirs) ?
                user.equals("root") ? PATH_SEPARATOR + user : PATH_SEPARATOR + "home" + PATH_SEPARATOR + user
                : dirs;
    }

    public SshModel( String name, String hostname, int port, String user, String password,
                    String temp, String charset, String forbid, String suffix, boolean discover, String dirs) {
        this.id = IdUtil.fastSimpleUUID();
        this.name = name;
        this.hostname = hostname;
        this.port = port;
        this.user = user;
        this.password = password;
        this.temp = temp;
        this.charset = StrUtil.isBlank(charset) ? "UTF-8" : charset;
        this.forbid = StrUtil.isBlank(forbid) ? "reboot" : forbid;
        this.suffix = StrUtil.isBlank(suffix) ? "txt" : suffix;
        this.discover = discover;
        // 空则按用户计算；非空则直接赋值
        this.dirs = StrUtil.isBlank(dirs) ?
                user.equals("root") ? PATH_SEPARATOR + user : PATH_SEPARATOR + "home" + PATH_SEPARATOR + user
                : dirs;
    }

    /**
     * @Description : 获取可操作根目录清单
     * @Param : []
     * @Return : java.util.List<java.lang.String>
     * @Author : gsq
     * @Date : 11:25 上午
     * @note : An art cell !
    **/
    public List<String> getDirToList() {
        return CollUtil.newArrayList(StrUtil.split(dirs, StrUtil.LF));
    }

    /**
     * @Description : 获取可编辑文件后缀名清单
     * @Param : []
     * @Return : java.util.List<java.lang.String>
     * @Author : gsq
     * @Date : 11:27 上午
     * @note : An art cell !
    **/
    public List<String> getSuffixList() {
        return CollUtil.newArrayList(StrUtil.split(suffix, StrUtil.LF));
    }

    /**
     * @Description : 获取禁止命令清单
     * @Param : []
     * @Return : java.util.List<java.lang.String>
     * @Author : gsq
     * @Date : 2:34 下午
     * @note : An art cell !
    **/
    public List<String> getForbidList() {
        return CollUtil.newArrayList(StrUtil.split(forbid, StrUtil.LF));
    }

    /**
     * @Description : 获取编码格式实体
     * @Param : []
     * @Return : java.nio.charset.Charset
     * @Author : gsq
     * @Date : 11:50 上午
     * @note : ⚠️ 默认编码格式为 UTF-8 !
    **/
    public Charset getCharsetInstance() {
        Charset charset;
        try {
            charset = Charset.forName(this.getCharset());
        } catch (Exception e) {
            charset = CharsetUtil.CHARSET_UTF_8;
        }
        return charset;
    }

}
