package cn.gsq.ssh.model;

import lombok.Getter;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.model.SshDocument
 *
 * @author : gsq
 * @date : 2022-06-08 17:58
 * @note : It's not technology, it's art !
 **/
@Getter
public class SshDocument extends SshFile {

    private final String size;  // 文件大小

    private final boolean edit; // 是否允许编辑

    public SshDocument(String name, String path, String modifyTime, String size, boolean edit) {
        super(name, path, modifyTime);
        this.size = size;
        this.edit = edit;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

}
