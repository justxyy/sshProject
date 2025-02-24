package cn.gsq.ssh.jsch;

import cn.gsq.ssh.SshConstant;
import cn.gsq.ssh.exception.SshDelException;
import cn.gsq.ssh.exception.SshDownloadException;
import cn.gsq.ssh.exception.SshUploadException;
import cn.gsq.ssh.model.SshDirectory;
import cn.gsq.ssh.model.SshDocument;
import cn.gsq.ssh.model.SshFile;
import cn.gsq.ssh.model.SshModel;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.ChannelType;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.jsch.SshBootstrap
 *
 * @author : gsq
 * @date : 2022-05-11 17:36
 * @note : It's not technology, it's art !
 **/
@Slf4j
public class SshBootstrap {

    @Getter
    protected final String id;

    @Getter
    protected final SshModel model;   // 链接信息

    /**
     * @Description : 构造函数
     * @Param : [model]
     * @Return :
     * @Author : gsq
     * @Date : 6:05 下午
     * @note : An art cell !
    **/
    public SshBootstrap(SshModel model) {
        this.id = model.getId();
        this.model = model;
    }

    /**
     * @Description : 判断链接是否可用
     * @Param : []
     * @Return : boolean
     * @Author : gsq
     * @Date : 3:03 下午
     * @note : ⚠️ 链接不可用、管理目录不可用都视为检测失败 !
    **/
    public boolean isAvailable() {
        boolean flag = true;
        Session session = getSshSesion();
        if(ObjectUtil.isNull(session)) {
            flag = false;
        } /*else {
            ChannelSftp channel = (ChannelSftp) JschUtil.openChannel(session, ChannelType.SFTP);
            for (String dir : StrUtil.split(this.model.getDirs(), StrUtil.LF)) {
                boolean exist = isFileExist(dir, channel);
                if(!exist) {
                    log.error(SshConstant.SSH_EXCEPTION_DIR, this.model.getHostname(), dir);
                    flag = false;
                    break;
                }
            }
            close(channel);
        }*/
        close(session);
        return flag;
    }

    /**
     * @Description : 根据sftp协议获取授权的根目录列表
     * @Param : []
     * @Return : java.util.List<javafx.util.Pair<java.lang.String,java.lang.Boolean>>
     * @Author : gsq
     * @Date : 2:11 下午
     * @note : ⚠️ 不存在的目录value是false !
    **/
    public Map<String, Boolean> getRootFiles() {
        Map<String, Boolean> files = new LinkedHashMap<>();
        Session session = getSshSesion();
        ChannelSftp channel = (ChannelSftp) JschUtil.openChannel(session, ChannelType.SFTP);
        for(String dir : StrUtil.split(this.model.getDirs(), StrUtil.LF)) {
            files.put(dir, isFileExist(dir, channel));
        }
        close(channel);
        close(session);
        return files;
    }

    /**
     * @Description : 根据sftp协议获取授权根目录下的子目录
     * @Param : [rootDir, children]
     * @Return : java.util.List<cn.gsq.ssh.model.SshFile>
     * @Author : gsq
     * @Date : 6:06 下午
     * @note : ⚠️ 参数children为空则直接查询根目录 !
    **/
    public List<SshFile> getFilesByRoot(String rootDir, String children) {
        Session session = getSshSesion();
        ChannelSftp channel = (ChannelSftp) JschUtil.openChannel(session, ChannelType.SFTP);
        List<SshFile> files = CollUtil.newArrayList();
        try {
            Vector<ChannelSftp.LsEntry> vector;
            if (StrUtil.isNotEmpty(children)) {
                String allPath = StrUtil.format(SshConstant.SSH_FILE_PATH, rootDir, children);
                allPath = FileUtil.normalize(allPath);
                vector = channel.ls(allPath);
            } else {
                vector = channel.ls(rootDir);
            }
            for (ChannelSftp.LsEntry lsEntry : vector) {
                String filename = lsEntry.getFilename();
                if (StrUtil.DOT.equals(filename) || StrUtil.DOUBLE_DOT.equals(filename)) {
                    continue;
                }
                if(StrUtil.startWith(filename, StrUtil.DOT) && !this.model.isDiscover()) {
                    continue;
                }
                String filetime = DateUtil.format(DateUtil.date(lsEntry.getAttrs().getMTime() * 1000L)
                        , DatePattern.NORM_DATETIME_MINUTE_PATTERN);
                String path = FileUtil.normalize(StrUtil.format(SshConstant.SSH_FILE_PATH, children, filename));
                if (lsEntry.getAttrs().isDir()) {
                    files.add(new SshDirectory(filename, path, filetime));
                } else {
                    String filesize = FileUtil.readableFileSize(lsEntry.getAttrs().getSize());
                    boolean isEdit = CollUtil.contains(this.model.getSuffixList(), FileUtil.getSuffix(filename));
                    files.add(new SshDocument(filename, path, filetime, filesize, isEdit));
                }
            }
        } catch (SftpException e) {
            log.error(SshConstant.SSH_EXCEPTION_LIST, this.model.getHostname(), rootDir);
            e.printStackTrace();
        } finally {
            close(channel);
            close(session);
        }
        return files;
    }

    /**
     * @Description : 根据sftp协议读取文件内容
     * @Param : [rootDir, children]
     * @Return : java.lang.String
     * @Author : gsq
     * @Date : 3:41 下午
     * @note : An art cell !
    **/
    public String getFileContent(String rootDir, String children) {
        Session session = null;
        Sftp sftp = null;
        String content = null;
        try {
            Charset charset = this.model.getCharsetInstance();
            session = getSshSesion();
            sftp = new Sftp(session, charset);
            String normalize = FileUtil.normalize(rootDir + StrUtil.SLASH + children);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            sftp.download(normalize, byteArrayOutputStream);
            content = new String(byteArrayOutputStream.toByteArray(), charset);
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_READ, this.model.getHostname(), e.getMessage());
            e.printStackTrace();
        } finally {
            close(sftp);
            close(session);
        }
        return content;
    }

    /**
     * @Description : 根据sftp协议修改文件内容
     * @Param : [rootDir, children, content]
     * @Return : boolean
     * @Author : gsq
     * @Date : 5:36 下午
     * @note : An art cell !
    **/
    public void updateFileContent(String rootDir, String children, String content) throws SshDelException {
        Session session = null;
        Sftp sftp = null;
        boolean flag = true;
        try {
            // 缓存文件到本地
            File file = FileUtil.file(this.model.getTemp(), children);
            FileUtil.writeString(content, file, this.getModel().getCharsetInstance());
            // 上传到服务器
            session = getSshSesion();
            sftp = new Sftp(session, this.model.getCharsetInstance());
            String normalize = FileUtil.normalize(rootDir + StrUtil.SLASH + children);
            sftp.upload(normalize, file);
            // 清除缓存缓存文件
            FileUtil.del(file);
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_UPDATE, this.model.getHostname(), e.getMessage());
            e.printStackTrace();
            throw new SshDelException("文件内容更新错误：" + e.getMessage());
        } finally {
            close(sftp);
            close(session);
        }
    }

    /**
     * @Description : 删除文件或者目录
     * @Param : [rootDir, children]
     * @Return : void
     * @Author : gsq
     * @Date : 11:12 上午
     * @note : ⚠️ 删除失败会抛出错误 !
    **/
    public void deleteFile(String rootDir, String children) throws SshDelException {
        children = FileUtil.normalize(children);
        if(StrUtil.equals(children, StrUtil.SLASH)) {
            throw new SshDelException(
                    StrUtil.format(SshConstant.SSH_EXCEPTION_ROOT, this.model.getHostname(), children)
            );
        } else {
            Session session = null;
            Sftp sftp = null;
            String normalize = null;
            try {
                session = getSshSesion();
                sftp = new Sftp(session, this.model.getCharsetInstance());
                normalize = FileUtil.normalize(rootDir + StrUtil.SLASH + children);
                delDocOrDir(sftp, normalize);
            } catch (Exception e) {
                log.error(SshConstant.SSH_EXCEPTION_DELETE, this.model.getHostname(), normalize, e.getMessage());
                e.printStackTrace();
                throw new SshDelException("文件删除错误：" + e.getMessage());
            } finally {
                close(sftp);
                close(session);
            }
        }
    }

    /**
     * @Description : 上传文件
     * @Param : [function, rootDir, children]
     * @Return : void
     * @Author : gsq
     * @Date : 3:00 下午
     * @note : ⚠️ 临时文件需要放在function指定的目录下，上传完成会自动删除 !
    **/
    public void uploadFile(Function<String, File> function, String rootDir, String children) throws SshUploadException {
        File file = function.apply(this.model.getTemp());
        String remotePath = FileUtil.normalize(rootDir + StrUtil.SLASH + children);
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSshSesion();
            channel = (ChannelSftp) JschUtil.openChannel(session, ChannelType.SFTP);
            channel.cd(remotePath);
            try (FileInputStream src = IoUtil.toStream(file)) {
                channel.put(src, file.getName());
            }
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_UPLOAD, this.model.getHostname(), file.getName(), e.getMessage());
            e.printStackTrace();
            throw new SshUploadException("文件上传错误：{}" + e.getMessage());
        } finally {
            close(channel);
            close(session);
            FileUtil.del(file);
        }
    }

    /**
     * @Description : 下载文件
     * @Param : [rootDir, children, outputStream]
     * @Return : void
     * @Author : gsq
     * @Date : 3:11 下午
     * @note : An art cell !
    **/
    public void downloadFile(String rootDir, String children, OutputStream outputStream) throws SshDownloadException {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = getSshSesion();
            channel = (ChannelSftp) JschUtil.openChannel(session, ChannelType.SFTP);
            String normalize = FileUtil.normalize(rootDir + StrUtil.SLASH + children);
            channel.get(normalize, outputStream);
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_DOWNLOAD, this.model.getHostname(), FileUtil.getName(children), e.getMessage());
            e.printStackTrace();
            throw new SshDownloadException("文件下载错误：{}" + e.getMessage());
        } finally {
            close(channel);
            close(session);
        }
    }

    /**
     * @Description : 在ssh远程服务主机上运行cmd命令
     * @Param : [cmd]
     * @Return : org.apache.commons.lang3.tuple.Triple<java.lang.String,java.lang.Boolean,java.lang.String>
     * @Author : gsq
     * @Date : 4:50 下午
     * @note : ⚠️ 左侧返回值, 中间运行结果, 右侧输出的错误信息 !
    **/
    public Triple<String, Boolean, String> execCommand(String cmd) {
        Session session = getSshSesion();
        Triple<String, Boolean, String> triple = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            if(ObjectUtil.isNotNull(session)) {
                String result = JschUtil.exec(session, cmd, model.getCharsetInstance(), stream);
                String error = new String(stream.toByteArray(), model.getCharsetInstance());
                triple = Triple.of(result, StrUtil.isBlank(error), error);
            }
            close(session);
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_EXEC, this.model.getHostname(), cmd, e.getMessage(), e);
        }
        return triple;
    }

    /**
     * @Description : 通过sftp上传文件
     * @Param : [destPath, filePath]
     * @Return : void
     * @Author : gsq
     * @Date : 6:05 下午
     * @note : An art cell !
    **/
    public void uploadFile(String destPath, String filePath) {
        Session session = getSshSesion();
        Sftp sftp = null;
        try {
            sftp = JschUtil.createSftp(session);
            sftp.upload(destPath, FileUtil.file(filePath));
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_UPLOADFILE, this.model.getHostname(), filePath, e.getMessage(), e);
        } finally {
            close(sftp);
            close(session);
        }
    }

    /**
     * @Description : 通过sftp上传本地文件到ssh服务器
     * @Param : [destPath, fileName, fileStream]
     * @Return : void
     * @Author : gsq
     * @Date : 6:16 下午
     * @note : An art cell !
    **/
    public void uploadFile(String destPath, String fileName, InputStream fileStream) {
        Session session = getSshSesion();
        Sftp sftp = null;
        try {
            sftp = JschUtil.createSftp(session);
            sftp.upload(destPath, fileName, fileStream);
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_UPLOADFILE, this.model.getHostname(), fileName, e.getMessage(), e);
        } finally {
            close(sftp);
            close(session);
        }
    }

    /**
     * @Description : 通过sftp下载文件到本地
     * @Param : [src, dest]
     * @Return : void
     * @Author : gsq
     * @Date : 5:26 下午
     * @note : An art cell !
    **/
    public void downloadFile(String src, String dest) {
        Session session = getSshSesion();
        Sftp sftp = null;
        try {
            sftp = JschUtil.createSftp(session);
            FileUtil.mkdir(FileUtil.getParent(dest, 1));
            sftp.download(src, FileUtil.file(dest));
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_DOWNLOADFILE, this.model.getHostname(), src, e.getMessage(), e);
        } finally {
            close(sftp);
            close(session);
        }
    }

    /**
     * @Description : 获取ssh会话
     * @Param : []
     * @Return : com.jcraft.jsch.Session
     * @Author : gsq
     * @Date : 2:22 下午
     * @note : ⚠️ 此时链接已经打开 !
    **/
    protected Session getSshSesion() {
        Session session = null;
        try {
            session = JschUtil.openSession(this.model.getHostname(), this.model.getPort(), this.model.getUser(),
                    this.model.getPassword(), this.model.getTimeout());
            session.setServerAliveInterval((int) TimeUnit.SECONDS.toMillis(5));
            session.setServerAliveCountMax(5);
        } catch (Exception e) {
            log.error(SshConstant.SSH_EXCEPTION_CONNECTION, this.model.getName());
            e.printStackTrace();
        }
        return session;
    }

    /**
     * @Description : 判断文件是否存在
     * @Param : [dir, channel]
     * @Return : boolean
     * @Author : gsq
     * @Date : 2:05 下午
     * @note : ⚠️ channel不会释放 !
    **/
    protected boolean isFileExist(String dir, ChannelSftp channel) {
        boolean flag = true;
        try {
            channel.ls(dir);
        } catch (SftpException e) {
            flag = false;
        }
        return flag;
    }

    /**
     * @Description : 删除文件夹或文件
     * @Param : [sftp, path]
     * @Return : boolean
     * @Author : gsq
     * @Date : 10:29 上午
     * @note : ⚠️ 不会释放sftp资源，保证清除文件 !
    **/
    protected void delDocOrDir(Sftp sftp, String path) {
        try {
            sftp.delDir(path);
        } catch (Exception e) {
            sftp.delFile(path);
        }
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

    /**
     * @Description : 关闭信道
     * @Param : [channel]
     * @Return : void
     * @Author : gsq
     * @Date : 5:06 下午
     * @note : An art cell !
    **/
    protected void close(Channel channel) {
        if(ObjectUtil.isNotNull(channel)) {
            JschUtil.close(channel);
        }
    }

    /**
     * @Description : 关闭sftp
     * @Param : []
     * @Return : void
     * @Author : gsq
     * @Date : 3:37 下午
     * @note : An art cell !
    **/
    protected void close(Sftp sftp) {
        if(ObjectUtil.isNotNull(sftp)) {
            IoUtil.close(sftp);
        }
    }

}
