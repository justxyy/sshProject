package cn.gsq.ssh.model;

import lombok.Getter;

/**
 * Project : galaxy
 * Class : cn.gsq.ssh.model.SshDirectory
 *
 * @author : gsq
 * @date : 2022-06-08 17:55
 * @note : It's not technology, it's art !
 **/
@Getter
public class SshDirectory extends SshFile {

    public SshDirectory(String name, String path, String modifyTime) {
        super(name, path, modifyTime);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

}
