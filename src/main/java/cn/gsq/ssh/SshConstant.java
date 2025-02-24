package cn.gsq.ssh;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.SshConstant
 *
 * @author : gsq
 * @date : 2022-06-10 17:45
 * @note : It's not technology, it's art !
 **/
public final class SshConstant {

    public static final String SSH_SOCKET_ENDPOINT = "/socket/galaxy/ssh";

    public static final String SSH_BANNER_IMG = "img/{}.txt";

    public static final String SSH_FILE_PATH = "{}/{}";

    public static final String SSH_EXCEPTION_DIR = "ssh实例{}的管理目录{}不可用";

    public static final String SSH_EXCEPTION_CONNECTION = "ssh链接{}不可用";

    public static final String SSH_EXCEPTION_ROOT = "ssh实例{}不能删除根目录：{}";

    public static final String SSH_EXCEPTION_LIST = "ssh实例{}获取授权目录{}下的文件列表错误";

    public static final String SSH_EXCEPTION_READ = "ssh实例{}读取文件内容错误：{}";

    public static final String SSH_EXCEPTION_UPDATE = "ssh实例{}修改文件内容错误：{}";

    public static final String SSH_EXCEPTION_DELETE = "ssh实例{}删除文件{}错误：{}";

    public static final String SSH_EXCEPTION_UPLOAD = "ssh实例{}上传文件{}错误：{}";

    public static final String SSH_EXCEPTION_DOWNLOAD = "ssh实例{}下载文件{}错误：{}";

    public static final String SSH_EXCEPTION_EXEC = "ssh实例{}执行命令{}错误：{}";

    public static final String SSH_EXCEPTION_UPLOADFILE = "ssh实例{}上传文件{}错误：{}";

    public static final String SSH_EXCEPTION_DOWNLOADFILE = "ssh实例{}下载文件{}错误：{}";

    public static final String SSH_EXCEPTION_STREAM = "ssh实例{}shell流获取错误：{}";

    public static final String SSH_EXCEPTION_SEND = "websocket发送消息失败：{}";

    public static final String SSH_EXCEPTION_WEBSOCKET = "websocket链接{}异常：{}";

    public static final String SSH_EXCEPTION_MONITOR = "ssh服务监听链接管道失败：{}";

    public static final String SSH_EXCEPTION_WRITE = "ssh服务写入链接管道失败：{}";

    public static final String SSH_WARN_SEND = "会话已经关闭，不能发送消息：{}";

    public static final String SSH_WARN_PERMISSIONS = "\n警告：没有执行该命令的权限！";

}
