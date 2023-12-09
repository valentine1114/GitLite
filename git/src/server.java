import Exceptions.NoSuchFileException;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class server {
    /**
     * 创建服务器
     *
     * @param args
     */
    public static void main(String[] args) {
        Socket socket = null;
        ServerSocket ss = null;
        try {
            //创建服务
            Scanner in = new Scanner(System.in);
            System.out.print("请输入服务器端口号：");
            int port = in.nextInt();
            ss = new ServerSocket(port);
            // 创建接收客户端socket对象
            System.out.println("等待客户端连接...");
            while (true) {
                //循环监听等待客户端的连接
                socket = ss.accept();
                System.out.println("当前服务器的端口号：" + ss.getLocalPort());
                System.out.println("服务器成功连接");
                System.out.println("当前路径为：" + System.getProperty("user.dir"));
                String implementation = utility.getMessage(socket);//读取命令
                switch (implementation) {
                    case "push"://如果消息是“push”，服务器将删除自己的 git 目录并从客户端接收新目录，然后执行 git reset 到最新的提交。
                        System.out.println("服务器执行push操作");
                        utility.receive(socket);
                        String commitStr = Commit.get_head_commit_id();
                        String[] list = {"git", "--hard", commitStr};
                        git.reset(list);
                        break;
                    case "pull"://如果消息是“pull”，服务器会将自己的 git 目录发送给客户端。
                        System.out.println("服务器执行pull操作");
                        if (!new File(utility.getGitDirPath().toString()).exists()) {
                            utility.sendMessage(socket, "文件不存在");
                            throw new NoSuchFileException();
                        } else utility.send(socket);
                        break;
                }
            }
        } catch (BindException e) {
            System.err.println("端口号已被使用，请重新输入~~");
            main(args);
        } catch (NoSuchFileException e) {
            System.err.println("当前仓库为空，请先建立仓库");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
