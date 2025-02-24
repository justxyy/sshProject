import ch.qos.logback.core.util.TimeUtil;
import cn.gsq.ssh.SshConstant;
import cn.gsq.ssh.jsch.SshBootstrap;
import cn.gsq.ssh.model.SshModel;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.ChannelType;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.extra.ssh.Sftp;
import cn.hutool.json.JSONUtil;
import com.jcraft.jsch.*;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Project : galaxy
 * Class : PACKAGE_NAME.SshTest
 *
 * @author : gsq
 * @date : 2022-05-11 10:29
 * @note : It's not technology, it's art !
 **/
@Slf4j
public class SshTest {

    @Test
    public void ssh_command01() {
        String cmd = "java -version";
        ByteOutputStream output = new ByteOutputStream();
        String result = JschUtil.exec(getSshSesion(),
                "source /etc/profile && source ~/.bash_profile && source ~/.bashrc && " + cmd,
                CharsetUtil.CHARSET_UTF_8,
                output);
        String error = new String(output.getBytes(), CharsetUtil.CHARSET_UTF_8);
        System.out.println(StrUtil.format("正常输出：{}", result));
        System.out.println(StrUtil.format("错误输出：{}", error));
        System.out.println("---------------------");
        SshBootstrap bootstrap = new SshBootstrap(
                new SshModel(IdUtil.fastSimpleUUID(), "测试", "10.0.13.157", 22,
                        "root", "admin1234@sugon", null, CharsetUtil.UTF_8,
                        null, null, true, null)
        );
        System.out.println(bootstrap.isAvailable());
        bootstrap.downloadFile("/opt/galaxy-5.1.1.tar.gz", "/Users/gsq/Downloads/test/aaa/t.tar.gz");
        System.out.println(JSONUtil.toJsonPrettyStr(bootstrap.execCommand("jps")));
    }

    @Test
    @SneakyThrows
    public void ssh_command02() {
        List<SshModel> sshModels = CollUtil.newArrayList(
            new SshModel("111111", "hahahah", "taie04", 2090, "root", "111",  "/tmp", "UTF-8",null, null, true, null),
            new SshModel("111111", "hahahah", "taie02", 2090, "root", "111",  "/tmp", "UTF-8",null, null, true, null),
            new SshModel("111111", "hahahah", "taie03", 2090, "root", "111",  "/tmp", "UTF-8",null, null, true, null),
            new SshModel("111111", "hahahah", "taie01", 2090, "root", "111",  "/tmp", "UTF-8",null, null, true, null),
            new SshModel("111111", "hahahah", "taie05", 2090, "root", "111",  "/tmp", "UTF-8",null, null, true, null)
        );
        sshModels.forEach(model -> System.out.println(model.getHostname()));
        System.out.println("-----------------------");
        sshModels = CollUtil.sort(sshModels, Comparator.comparing(SshModel::getHostname));
        sshModels.forEach(model -> System.out.println(model.getHostname()));
    }

    @Test
    public void ssh_download() {
        Sftp sftp = JschUtil.createSftp(getSshSesion());
        long current = DateUtil.current();
//        sftp.download("/opt/galaxy-5.1.1.tar.gz", new File("/Users/gsq/Downloads/core.jar"));
//        sftp.upload("/opt", FileUtil.file("/Users/gsq/Downloads/core.jar"));
        long now = DateUtil.current();
        System.out.println(DateUtil.date(current).toString());
        System.out.println(DateUtil.date(now).toString());
    }

    @Test
    public void ssh_command03() {
        File file = FileUtil.file("/Users/gsq/Downloads/test/aaa", "/bbb/ccc", "/ppp/presto.log");
        FileUtil.writeString("我是你父亲", file, CharsetUtil.CHARSET_UTF_8);
        System.out.println(FileUtil.getUserHomePath());

//        System.out.println(FileUtil.mkdir("/Users/gsq/Downloads/test/sdb/taie.tar.gz"));
    }

    private Session getSshSesion() {
        String hostname = "10.0.13.157";
        Session session = null;
        try {
            session = JschUtil.openSession(hostname, 22,
                    "root", "admin1234@sugon", 10000);
            session.setServerAliveInterval((int) TimeUnit.SECONDS.toMillis(5));
            session.setServerAliveCountMax(5);
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_CONNECTION, hostname);
        }
        return session;
    }

}
