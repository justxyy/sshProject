package cn.gsq.ssh.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.config.SshProperties
 *
 * @author : gsq
 * @date : 2022-05-13 17:54
 * @note : It's not technology, it's art !
 **/
@Getter
@Setter
@ConfigurationProperties(prefix="galaxy.ssh")
public class SshProperties {

    private String home = FileUtil.getUserHomePath() + StrUtil.SLASH + "galaxy" + StrUtil.SLASH + "ssh";

}
